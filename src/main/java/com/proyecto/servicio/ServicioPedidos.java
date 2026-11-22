package com.proyecto.servicio;

import com.proyecto.modelo.CarritoItem;
import com.proyecto.modelo.EstadoPedido;
import com.proyecto.modelo.ItemPedido;
import com.proyecto.modelo.Pedido;
import com.proyecto.modelo.Producto;
import com.proyecto.persistencia.RepositorioPedidos;
import com.proyecto.persistencia.RepositorioProductos;
import com.proyecto.util.GeneradorId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de pedidos del cliente: convierte el carrito en un pedido
 * (validando y descontando stock), lista los pedidos propios y permite
 * cancelar los que aún estén pendientes (restaurando el stock).
 */
public class ServicioPedidos {

    private final RepositorioPedidos repositorioPedidos;
    private final RepositorioProductos repositorioProductos;

    public ServicioPedidos(RepositorioPedidos repositorioPedidos,
                           RepositorioProductos repositorioProductos) {
        this.repositorioPedidos = repositorioPedidos;
        this.repositorioProductos = repositorioProductos;
    }

    /**
     * Finaliza la compra: valida el stock de todos los ítems <b>antes</b> de
     * tocar nada (la compra es todo-o-nada), descuenta el stock, crea el
     * pedido con la fotografía de precios y lo deja PENDIENTE.
     *
     * @throws ExcepcionNegocio si el carrito está vacío o algún producto no
     *                          existe o no tiene stock suficiente.
     */
    public Pedido crearPedido(List<CarritoItem> items, String clienteId, String clienteNombre) {
        if (items == null || items.isEmpty()) {
            throw new ExcepcionNegocio("El carrito está vacío");
        }

        // 1. Validación completa antes de mutar nada (compra todo-o-nada).
        List<ItemPedido> lineas = new ArrayList<>();
        for (CarritoItem item : items) {
            Producto producto = repositorioProductos.buscarPorId(item.getProductoId())
                    .orElseThrow(() -> new ExcepcionNegocio(
                            "Un producto del carrito ya no está disponible"));
            if (producto.getStock() < item.getCantidad()) {
                throw new ExcepcionNegocio("Stock insuficiente de \"" + producto.getTitulo()
                        + "\" (quedan " + producto.getStock() + ")");
            }
            lineas.add(new ItemPedido(producto.getId(), producto.getTitulo(),
                    producto.getVendedorId(), producto.getPrecio(), item.getCantidad()));
        }

        // 2. Descuento el stock de cada producto validado.
        for (CarritoItem item : items) {
            Producto producto = repositorioProductos.buscarPorId(item.getProductoId()).orElseThrow();
            producto.setStock(producto.getStock() - item.getCantidad());
            repositorioProductos.guardar(producto);
        }

        // 3. Creo el pedido con la fotografía de precios del momento.
        double subtotal = lineas.stream().mapToDouble(ItemPedido::subtotal).sum();
        double impuesto = redondear(subtotal * ServicioCarrito.TASA_IMPUESTO);
        Pedido pedido = new Pedido(
                GeneradorId.generar("PED"),
                clienteId,
                clienteNombre,
                LocalDate.now().toString(),
                EstadoPedido.PENDIENTE,
                lineas,
                redondear(subtotal),
                impuesto,
                redondear(subtotal + impuesto));
        repositorioPedidos.guardar(pedido);
        return pedido;
    }

    /**
     * Pedidos del cliente, el más reciente primero.
     */
    public List<Pedido> listarPorCliente(String clienteId) {
        List<Pedido> propios = new ArrayList<>(repositorioPedidos.leerTodos().stream()
                .filter(p -> p.getClienteId().equals(clienteId))
                .toList());
        java.util.Collections.reverse(propios);
        return propios;
    }

    /**
     * Cancela un pedido propio que siga PENDIENTE y restaura el stock de
     * cada ítem (si el producto aún existe).
     *
     * @throws ExcepcionNegocio si el pedido no existe, no es del cliente o
     *                          ya no se puede cancelar.
     */
    public void cancelar(String pedidoId, String clienteId) {
        Pedido pedido = repositorioPedidos.buscarPorId(pedidoId)
                .orElseThrow(() -> new ExcepcionNegocio("El pedido no existe"));
        if (!pedido.getClienteId().equals(clienteId)) {
            throw new ExcepcionNegocio("Ese pedido no te pertenece");
        }
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ExcepcionNegocio("Solo se pueden cancelar pedidos pendientes");
        }
        for (ItemPedido item : pedido.getItems()) {
            repositorioProductos.buscarPorId(item.getProductoId()).ifPresent(producto -> {
                producto.setStock(producto.getStock() + item.getCantidad());
                repositorioProductos.guardar(producto);
            });
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        repositorioPedidos.guardar(pedido);
    }

    // ---------------------------------------------------------------- módulo del vendedor

    /**
     * Pedidos pendientes que contienen al menos un ítem publicado por el
     * vendedor, ordenados de más antiguo a más reciente (orden de llegada).
     * Es la cola de despacho del vendedor.
     */
    public java.util.List<Pedido> listarPendientesPorVendedor(String vendedorId) {
        return repositorioPedidos.leerTodos().stream()
                .filter(p -> p.getEstado() == EstadoPedido.PENDIENTE)
                .filter(p -> p.getItems().stream()
                        .anyMatch(i -> i.getVendedorId().equals(vendedorId)))
                .sorted(java.util.Comparator.comparing(Pedido::getFecha))
                .toList();
    }

    /**
     * Despacha el siguiente pedido de la cola: el más antiguo entre los
     * pendientes que tengan ítems del vendedor. Pasa su estado de
     * PENDIENTE a ENVIADO.
     *
     * @throws ExcepcionNegocio si no hay pedidos pendientes para este vendedor.
     */
    public Pedido despacharSiguiente(String vendedorId) {
        java.util.List<Pedido> pendientes = listarPendientesPorVendedor(vendedorId);
        if (pendientes.isEmpty()) {
            throw new ExcepcionNegocio("No hay pedidos pendientes en tu cola");
        }
        Pedido siguiente = pendientes.get(0);
        siguiente.setEstado(EstadoPedido.ENVIADO);
        repositorioPedidos.guardar(siguiente);
        return siguiente;
    }

    /**
     * Marca un pedido como ENTREGADO.
     *
     * @throws ExcepcionNegocio si el pedido no existe, no tiene ítems del
     *                          vendedor, o no está ENVIADO (solo se puede
     *                          entregar lo que ya fue despachado).
     */
    public void marcarEntregado(String pedidoId, String vendedorId) {
        Pedido pedido = repositorioPedidos.buscarPorId(pedidoId)
                .orElseThrow(() -> new ExcepcionNegocio("El pedido no existe"));
        boolean tieneItemDelVendedor = pedido.getItems().stream()
                .anyMatch(i -> i.getVendedorId().equals(vendedorId));
        if (!tieneItemDelVendedor) {
            throw new ExcepcionNegocio("Ese pedido no contiene tus productos");
        }
        if (pedido.getEstado() != EstadoPedido.ENVIADO) {
            throw new ExcepcionNegocio("Solo se pueden entregar pedidos despachados");
        }
        pedido.setEstado(EstadoPedido.ENTREGADO);
        repositorioPedidos.guardar(pedido);
    }

    /**
     * El cliente confirma haber recibido su pedido. Pasa el estado de
     * ENVIADO a ENTREGADO, cerrando el flujo de venta.
     *
     * @throws ExcepcionNegocio si el pedido no existe, no es del cliente,
     *                          o no está ENVIADO (solo se puede recibir lo
     *                          que ya fue despachado).
     */
    public void recibirPedido(String pedidoId, String clienteId) {
        Pedido pedido = repositorioPedidos.buscarPorId(pedidoId)
                .orElseThrow(() -> new ExcepcionNegocio("El pedido no existe"));
        if (!pedido.getClienteId().equals(clienteId)) {
            throw new ExcepcionNegocio("Ese pedido no te pertenece");
        }
        if (pedido.getEstado() != EstadoPedido.ENVIADO) {
            throw new ExcepcionNegocio("Solo se pueden recibir pedidos despachados");
        }
        pedido.setEstado(EstadoPedido.ENTREGADO);
        repositorioPedidos.guardar(pedido);
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
