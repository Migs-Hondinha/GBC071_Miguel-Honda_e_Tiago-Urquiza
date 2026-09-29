import java.util.List;

public class TesteScanner {
    static int falhas = 0;
    static void testar(String nome, String entrada, String esperado) {
        StringBuilder sb = new StringBuilder();
        List<Token> tokens = new Scanner(entrada).tokenizar();
        for (Token t : tokens) {
            if (t.tipo != TokenType.EOF) {
                sb.append(t.tipo).append("(").append(t.lexema).append(") ");
            }
        }
        String obtido = sb.toString().trim();

        if (obtido.equals(esperado)) {
            System.out.println("[CERTO] " + nome + " -> " + obtido + "\n");
        } else {
            falhas++;
            System.out.println("[X] " + nome + " -> " + obtido);
            System.out.println(" esperado: " + esperado);
            System.out.println(" obtido: " + obtido);
        }
    }

    public static void main(String[] args) {
        // Testes corretos
        testar("identificador", "maya_prof", "IDENTIFICADOR(maya_prof)");
        testar("palavra reservada", "while", "PALAVRA_RESERVADA(while)");
        testar("string", "\"Bom dia\"", "STRING(Bom dia)");
        testar("operador", "<=", "OPERADOR(<=)");
        testar("numero", "3.14", "NUMERO(3.14)");

        // Erros 
        testar("string nao fechada (EOF)", "\"abc", "ERRO(\"abc)");
        testar("string nao fechada (fim da linha)", "\"abc\nx", "ERRO(\"abc) IDENTIFICADOR(x)");
        testar("caractere invalido", "a @ b", "IDENTIFICADOR(a) ERRO(@) IDENTIFICADOR(b)");

        // Comentario e Delimitador
        testar("teste comentario e delimitador",
            "int total = 10; // inicio\n/* bloco */ total = total + 1.2;",
            "PALAVRA_RESERVADA(int) IDENTIFICADOR(total) OPERADOR(=) NUMERO(10) DELIMITADOR(;) "
            + "IDENTIFICADOR(total) OPERADOR(=) IDENTIFICADOR(total) OPERADOR(+) NUMERO(1.2) DELIMITADOR(;)");

        System.out.println(falhas == 0 ? "\n Todos os testes passaram." : "\n" + falhas + " teste(s) falharam.");
    }
}