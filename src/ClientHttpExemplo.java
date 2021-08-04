import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ClientHttpExemplo {
    static ExecutorService executor = Executors.newFixedThreadPool(6, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            System.out.println("Nova Thread criada " + (thread.isDaemon() ? "daemon" : "") + "Thread group " +thread.getThreadGroup());
            return thread;
        }
    });
    public static void main(String[] args) throws IOException, InterruptedException {
//        connectAndPrintUrlJavaOracle();
//        connectAndPrintUrlJavaHttpOracle();
        connectAkamaisHttp11Client();
//        connectAkamaisHttp2Client();
    }


    private static void connectAndPrintUrlJavaOracle() {

        //forma antiga
        try {
            var url = new URL("https://docs.oracle.com/javase/10/language/");
            var urlConnection= url.openConnection();
            var bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            System.out.println(bufferedReader.lines().collect(Collectors.joining()).replaceAll(">", ">\n"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void connectAndPrintUrlJavaHttpOracle() throws IOException, InterruptedException{

        //nova forma
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(URI.create("https://docs.oracle.com/javase/10/language/"))
                .build();
         HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status code :: " + response.statusCode() );
        System.out.println("Headers response :: " + response.headers());
        System.out.println(response.body());
    }

    private static void connectAkamaisHttp11Client(){
        System.out.println("Running HTTP/1.1 example ...");
        try{
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .proxy(ProxySelector.getDefault())
                    .build();
            long start = System.currentTimeMillis();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://http2.akamai.com/demo/h2_demo_frame.html"))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status code :: " + response.statusCode());
            System.out.println("Headers response :: " + response.headers());
            String responseBody = response.body();
            System.out.println(responseBody);

            List<Future<?>> futures = new ArrayList<>();

            responseBody
                    .lines()
                    .filter(line -> line.trim().startsWith("<img height"))
                    .map(line -> line.substring(line.indexOf("src='")+5, line.indexOf("/>")))
                    .forEach(image-> {
                        Future<?> imgFuture = executor.submit(() -> {
                           HttpRequest imgRequest = HttpRequest.newBuilder()
                                   .uri(URI.create("https://http2.akamai.com" + image))
                                   .build();

                            try {
                                HttpResponse<String> imageResponse = httpClient.send(imgRequest, HttpResponse.BodyHandlers.ofString());
                                System.out.println("Imagem carregada :: " + image + ", status code "+ imageResponse.statusCode());
                            } catch (IOException | InterruptedException e) {
                                System.out.println("Mensagem de erro durante requisição para recuperar image "+ e.getMessage());
                            }
                        });
                        futures.add(imgFuture);
                        System.out.println("Submetidas um futuro para imagem :: " + image);
                    });

            futures.forEach(f-> {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Erro ao esperar carregar imagem do futuro ");
                }
            });

            long end = System.currentTimeMillis();
            System.out.println("Tempo de carregamento total :: "+ (end+start) + "ms");
        } catch (IOException | InterruptedException e) {
            System.out.println("Erro na execucao " + e.getMessage());
        } finally {
            executor.shutdown();
        }

    }

    private static void connectAkamaisHttp2Client() {
        System.out.println("Running HTTP/2 example ...");
        try{
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .proxy(ProxySelector.getDefault())
                    .build();
            long start = System.currentTimeMillis();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://http2.akamai.com/demo/h2_demo_frame.html"))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status code :: " + response.statusCode());
            System.out.println("Headers response :: " + response.headers());
            String responseBody = response.body();
            System.out.println(responseBody);

            List<Future<?>> futures = new ArrayList<>();

            responseBody
                    .lines()
                    .filter(line -> line.trim().startsWith("<img height"))
                    .map(line -> line.substring(line.indexOf("src='")+5, line.indexOf("/>")))
                    .forEach(image-> {
                        Future<?> imgFuture = executor.submit(() -> {
                            HttpRequest imgRequest = HttpRequest.newBuilder()
                                    .uri(URI.create("https://http2.akamai.com" + image))
                                    .build();

                            try {
                                HttpResponse<String> imageResponse = httpClient.send(imgRequest, HttpResponse.BodyHandlers.ofString());
                                System.out.println("Imagem carregada :: " + image + ", status code "+ imageResponse.statusCode());
                            } catch (IOException | InterruptedException e) {
                                System.out.println("Mensagem de erro durante requisição para recuperar image "+ e.getMessage());
                            }
                        });
                        futures.add(imgFuture);
                        System.out.println("Submetidas um futuro para imagem :: " + image);
                    });

            futures.forEach(f-> {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Erro ao esperar carregar imagem do futuro ");
                }
            });

            long end = System.currentTimeMillis();
            System.out.println("Tempo de carregamento total :: "+ (end+start) + "ms");
        } catch (IOException | InterruptedException e) {
            System.out.println("Erro na execucao " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
