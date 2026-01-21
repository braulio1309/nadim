/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.nadim;

import javax.swing.SwingUtilities;

/**
 *
 * @author edwin
 */
public class Nadim {

     public static void main(String[] args) {
        SwingUtilities.invokeLater(Nadim::crearVentana);
    }

    private static void crearVentana() {
        VentanaJuego ventana = new VentanaJuego();
        ventana.setVisible(true);
    }
}
