import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Clase Callable que procesa una tarea
class ProcesadorTarea implements Callable<String> {
    private final int id;
    private final int tiempoProcesamiento; // en milisegundos

    public ProcesadorTarea(int id, int tiempoProcesamiento) {
        this.id = id;
        this.tiempoProcesamiento = tiempoProcesamiento;
    }

    @Override
    public String call() throws Exception {
        System.out.println("Tarea " + id + " iniciada por: " + Thread.currentThread().getName());

        // Simular procesamiento
        Thread.sleep(tiempoProcesamiento);

        // Simular posible error en algunas tareas
        if (id % 4 == 0) {
            throw new RuntimeException("Error simulado en tarea " + id);
        }

        String resultado = "Tarea " + id + " completada en " + tiempoProcesamiento + "ms";
        System.out.println(resultado);
        return resultado;
    }
}

class EjemploCompletoCallableFuture {

    public static void main(String[] args) {
        // Crear un pool de hilos con 3 hilos
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<String>> futures = new ArrayList<>();

        System.out.println("=== INICIANDO PROCESAMIENTO DE TAREAS ===\n");

        // Enviar 10 tareas para ejecución
        for (int i = 1; i <= 10; i++) {
            int tiempo = (int) (Math.random() * 2000) + 500; // 500-2500ms
            Callable<String> tarea = new ProcesadorTarea(i, tiempo);

            // Submit devuelve un Future inmediatamente
            Future<String> future = executor.submit(tarea);
            futures.add(future);

            System.out.println("Tarea " + i + " enviada para procesamiento");
        }

        System.out.println("\n=== MONITOREANDO RESULTADOS ===\n");

        // Procesar los resultados
        for (int i = 0; i < futures.size(); i++) {
            Future<String> future = futures.get(i);
            int numeroTarea = i + 1;

            try {
                // Intentar obtener resultado con timeout de 3 segundos
                String resultado = future.get(3, TimeUnit.SECONDS);
                System.out.println("✓ Éxito en tarea " + numeroTarea + ": " + resultado);

            } catch (TimeoutException e) {
                System.out.println("✗ Tarea " + numeroTarea + " excedió el tiempo límite");
                future.cancel(true); // Cancelar si está demorando mucho

            } catch (InterruptedException e) {
                System.out.println("✗ Tarea " + numeroTarea + " interrumpida");
                Thread.currentThread().interrupt();

            } catch (ExecutionException e) {
                System.out.println("✗ Error en tarea " + numeroTarea + ": " + e.getCause().getMessage());

            } catch (Exception e) {
                System.out.println("✗ Error desconocido en tarea " + numeroTarea + ": " + e.getMessage());
            }
        }

        // Verificar estado final de las tareas
        System.out.println("\n=== ESTADO FINAL DE TAREAS ===");
        for (int i = 0; i < futures.size(); i++) {
            Future<String> future = futures.get(i);
            System.out.println("Tarea " + (i + 1) + ": " +
                    (future.isDone() ? "COMPLETADA" : "PENDIENTE") +
                    (future.isCancelled() ? " (CANCELADA)" : ""));
        }

        // Apagar el executor
        executor.shutdown();

        try {
            // Esperar a que todas las tareas terminen
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("\n=== PROCESAMIENTO COMPLETADO ===");
    }
}

// Ejemplo adicional: Callable con retorno de diferentes tipos
class Calculadora implements Callable<Double> {
    private final double numero;

    public Calculadora(double numero) {
        this.numero = numero;
    }

    @Override
    public Double call() throws Exception {
        Thread.sleep(1000); // Simular cálculo complejo
        return Math.sqrt(numero) * Math.PI;
    }
}

class EjemploCalculos {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();

        // Múltiples cálculos en paralelo
        Future<Double> futuro1 = executor.submit(new Calculadora(16));
        Future<Double> futuro2 = executor.submit(new Calculadora(25));
        Future<Double> futuro3 = executor.submit(new Calculadora(36));

        // Hacer otras cosas mientras se calcula...
        System.out.println("Realizando otras operaciones...");

        // Obtener resultados cuando se necesiten
        System.out.println("Resultado 1: " + futuro1.get());
        System.out.println("Resultado 2: " + futuro2.get());
        System.out.println("Resultado 3: " + futuro3.get());

        executor.shutdown();
    }
}