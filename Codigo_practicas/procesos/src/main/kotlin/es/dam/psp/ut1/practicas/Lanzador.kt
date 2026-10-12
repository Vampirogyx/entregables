package es.dam.psp.ut1.practicas

import es.dam.psp.ut1.ejemplos.Jvm
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

// Necesitarás además: import java.io.File y import java.util.concurrent.TimeUnit

/**
 * Lanzador.kt · Práctica 3 · Lanzador de órdenes. Completa los TODO 3.x (letras de la práctica).
 *
 * Apuntes UT-1, apartados 5.1 (ProcessBuilder, Jvm.comandoShell), 5.3 (leer la salida),
 * 6.1 (waitFor con límite, destroy) y 6.2 (no bloquearse leyendo).
 * Cómo ejecutarlo: triángulo ▶ junto a main, sin argumentos; escribe las órdenes en la consola
 * de IntelliJ (p. ej. "echo hola", "ls noexiste", "sleep 10" o, en Windows, "ping -n 11 127.0.0.1").
 *
 * Bucle: pide una orden al usuario, la ejecuta como proceso hijo y muestra
 *   - su salida (stdout y stderr juntas), con cada línea numerada (3.a),
 *   - su código de salida y el tiempo que ha tardado en milisegundos (3.b).
 * Si la orden tarda más de TIMEOUT_S segundos, se termina el proceso y se avisa (3.c).
 * Cada ejecución se añade a historial.txt con el formato:  orden;codigo;milisegundos (3.d)
 * La orden "salir" termina el programa.
 */

const val TIMEOUT_S = 5L   // segundos que se deja trabajar a cada orden

fun main() {
    println("Lanzador PSP · sistema: ${System.getProperty("os.name")} · escribe 'salir' para terminar")
    while (true) {
        print("> ")
        val orden = readlnOrNull()?.trim() ?: break   // null = fin de la entrada (Ctrl+D)
        if (orden == "salir") break
        if (orden.isEmpty()) continue

        // TODO 3.c (preparación): construye el ProcessBuilder con Jvm.comandoShell(orden) y
        //         redirectErrorStream(true). Problema: si lees la salida con readLines() y el hijo no
        //         termina nunca, el padre se queda bloqueado leyendo y el timeout no llega a comprobarse
        //         (apuntes, apartado 6.2). Solución sin hilos: redirige la salida a un fichero temporal
        //         (File.createTempFile("lanzador", ".txt") + redirectOutput(...)).

        val tempFile = File.createTempFile("lanzador", ".txt")

        val pb = ProcessBuilder(Jvm.comandoShell(orden)).apply {
            redirectErrorStream(true) // Une la salida estándar y la de error (Apartado A)
            redirectOutput(tempFile)  // Redirige al fichero temporal para que el padre no se bloquee
        }

        var codigoSalida = 0
        var proceso: Process? = null

        // TODO 3.b: arráncalo y mide el tiempo desde start() hasta que termina
        //           (System.nanoTime() antes y después, o measureTime { }).

        val tiempoTranscurrido = measureTimeMillis {
            try {
                proceso = pb.start()

        // TODO 3.c: espera como máximo TIMEOUT_S segundos (waitFor(TIMEOUT_S, TimeUnit.SECONDS)); si no
        //           ha terminado, termina también a sus descendientes (descendants()) y luego destroy();
        //           si sigue vivo, destroyForcibly(). En ese caso el código que guardes será -1.

                val terminadoATiempo = proceso?.waitFor(TIMEOUT_S, TimeUnit.SECONDS) ?: false

                if (!terminadoATiempo) {
                    println("Aviso: El proceso ha excedido el límite de $TIMEOUT_S segundos.")

                    proceso?.descendants()?.forEach { desc -> desc.destroy() }
                    proceso?.destroy()

                    if (proceso?.isAlive == true) {
                        proceso?.destroyForcibly()
                    }
                    codigoSalida = -1
                } else {

                    codigoSalida = proceso?.exitValue() ?: -1
                }
            } catch (e: Exception) {
                println("Error al intentar ejecutar la orden: ${e.message}")
                codigoSalida = -1
            }
        }

        // TODO 3.a: lee el fichero temporal y muestra cada línea numerada. Bórralo después.

        if (tempFile.exists()) {
            var numeroLinea = 1
            tempFile.forEachLine { linea ->
                println("$numeroLinea: $linea")
                numeroLinea++
            }
            tempFile.delete()
        }

        // TODO 3.b: muestra "[código X · Y ms]".

        println("[código $codigoSalida · $tiempoTranscurrido ms]")

        // TODO 3.d: añade la línea "orden;codigo;milisegundos" a historial.txt (appendText).

        val historialFile = File("historial.txt")
        historialFile.appendText("$orden;$codigoSalida;$tiempoTranscurrido\n")

    }
    println("Hasta luego")
}
