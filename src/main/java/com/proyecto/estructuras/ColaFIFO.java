package com.proyecto.estructuras;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * Cola FIFO ({@code First-In, First-Out}): el primer elemento en entrar es el
 * primero en salir. Es el comportamiento esperado para atender pedidos en
 * orden de llegada, sin "piches" ni prioridades ocultas.
 *
 * <p>Operaciones:</p>
 * <ul>
 *   <li>{@link #encolar}: añade al final (atrás de la cola).</li>
 *   <li>{@link #desencolar}: quita y devuelve el primero (delante de la cola).</li>
 *   <li>{@link #primero}: mira al siguiente sin quitarlo.</li>
 * </ul>
 *
 * @param <T> tipo de los elementos en cola.
 */
public class ColaFIFO<T> {

    private final Deque<T> cola = new ArrayDeque<>();

    /**
     * Añade un elemento al final de la cola.
     *
     * @param elemento elemento a encolar.
     */
    public void encolar(T elemento) {
        cola.addLast(elemento);
    }

    /**
     * Quita y devuelve el primer elemento (el que lleva más tiempo esperando).
     *
     * @return el elemento desencolado.
     * @throws NoSuchElementException si la cola está vacía.
     */
    public T desencolar() {
        if (cola.isEmpty()) {
            throw new NoSuchElementException("La cola está vacía");
        }
        return cola.removeFirst();
    }

    /**
     * Devuelve el primer elemento sin quitarlo, o null si la cola está vacía.
     */
    public T primero() {
        return cola.isEmpty() ? null : cola.peekFirst();
    }

    /**
     * true si no hay elementos en la cola.
     */
    public boolean estaVacia() {
        return cola.isEmpty();
    }

    /**
     * Cantidad de elementos esperando en la cola.
     */
    public int tamaño() {
        return cola.size();
    }

    /**
     * Elimina todos los elementos de la cola.
     */
    public void vaciar() {
        cola.clear();
    }
}
