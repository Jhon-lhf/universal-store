package com.proyecto.estructuras;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la cola FIFO (First-In, First-Out): el primero en entrar es el
 * primero en salir.
 */
class ColaFIFOTest {

    @Test
    void colaVaciaEstaVacia() {
        ColaFIFO<String> cola = new ColaFIFO<>();
        assertTrue(cola.estaVacia());
        assertEquals(0, cola.tamaño());
        assertNull(cola.primero());
    }

    @Test
    void desencolarColaVaciaLanzaExcepcion() {
        ColaFIFO<String> cola = new ColaFIFO<>();
        assertThrows(NoSuchElementException.class, cola::desencolar);
    }

    @Test
    void primeroEsElPrimeroEnEntrar() {
        ColaFIFO<String> cola = new ColaFIFO<>();
        cola.encolar("primero");
        cola.encolar("segundo");
        cola.encolar("tercero");

        assertEquals(3, cola.tamaño());
        assertEquals("primero", cola.primero()); // no lo quita
        assertEquals(3, cola.tamaño()); // todavía hay 3
    }

    @Test
    void desencolarRespetaOrdenFIFO() {
        ColaFIFO<String> cola = new ColaFIFO<>();
        cola.encolar("A");
        cola.encolar("B");
        cola.encolar("C");

        assertEquals("A", cola.desencolar());
        assertEquals("B", cola.desencolar());
        assertEquals("C", cola.desencolar());
        assertTrue(cola.estaVacia());
    }

    @Test
    void vaciarLimpiaLaCola() {
        ColaFIFO<String> cola = new ColaFIFO<>();
        cola.encolar("A");
        cola.encolar("B");
        cola.vaciar();
        assertTrue(cola.estaVacia());
        assertNull(cola.primero());
    }
}
