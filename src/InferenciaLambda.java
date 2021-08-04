import java.util.function.Function;

public class InferenciaLambda{
    public static void main(String[] args) {
        Function<Integer, Double> divisaoPor2 = (var numero) -> Double.valueOf(numero/2);
        System.out.println(divisaoPor2.apply(84556));
    }
}
