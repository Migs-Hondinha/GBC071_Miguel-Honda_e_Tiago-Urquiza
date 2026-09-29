import java.util.ArrayList;
import java.util.List;

public class Scanner {
    private final String source;
    private int posicao = 0;
    private int linha = 1;
    private int coluna = 1;

    //Palavras Reservadas: 
    private static final String[] palavrasReservadas = {
        "if", "else", "for", "while", "int", "float", "double", 
        "string", "exit", "return", "class", "bool", "char"
    };

    public Scanner(String source) {
        this.source = source;
    }

    private boolean hasNext() {
        return posicao < source.length();
    }

    private char avancar() {
        char x = source.charAt(posicao++);
        if (x == '\n') {
            linha++;
            coluna = 1;
        } else {
            coluna++;
        }
        return x;
    }

    private char peek() {
        if (!hasNext()) return '\0';
        return source.charAt(posicao);
    }

    private char lookahead(int n){
        if (posicao + n >= source.length()) return '\0';
        return source.charAt(posicao + n);
    }

    // ----------------------------------------------------------------------------------
    private boolean ehLetra(char x) {
        return (x >= 'a' && x <= 'z') || (x >= 'A' && x <= 'Z');
    }
    private boolean ehDigito(char x) {
        return (x >= '0' && x <= '9');
    } 

    private boolean ehLetraOuDigitoOuSublinhado(char x) {
        return ehLetra(x) || ehDigito(x) || x == '_';
    }

    private boolean ehOperador(char x) {
        return x == '=' || x == '!' || x == '<' || x == '>' || x == '&' 
            || x == '|' || x == '+' || x == '-' || x == '*' || x == '/';
    }

    private boolean ehDelimitador(char x) {
        return x == '(' || x == ')' || x == '{' || x == '}' || x == ';' || x == ',';
    }

    private boolean ehEspaco(char x) {
        return x == ' ' || x == '\t' || x == '\r' || x == '\n';
    }

    private boolean ehPalavraReservada(String s) {
        for (String p : palavrasReservadas) {
            if (p.equals(s)){
                return true;
            }
        }
        return false;
    }

    private void pularEspacosEComentarios() {
        while (hasNext()) {
            char x = peek();

            if (ehEspaco(x)) {
                avancar();
            }
            // Comentário de linha //
            else if (x == '/' && lookahead(1) == '/') {
                avancar();
                avancar();
                while (hasNext() && peek() != '\n') {
                    avancar();
                }
            }
            // Comentário de bloco /* ... */
            else if (x == '/' && lookahead(1) == '*') {
                int linhaInicio = linha;
                int colunaInicio = coluna;
                avancar(); 
                avancar(); 
                boolean fechou = false;
                while (hasNext()) {
                    if (peek() == '*' && lookahead(1) == '/') {
                        avancar();
                        avancar();
                        fechou = true;
                        break;
                    }
                    avancar();
                }

                if (!fechou) {
                    System.err.println("Erro Léxico: comentário de bloco não fechado iniciado na linha " 
                    + linhaInicio + ", coluna " + colunaInicio);
                }
            }
            else {
                break;
            }
        }
    }
// ----------------------------------------------------------------------------------
    private Token scanReservadaOuIdentificador() {
        int l = linha;
        int c = coluna;
        int inicio = posicao;
        avancar(); // START -> IN_ID

        while (hasNext() && ehLetraOuDigitoOuSublinhado(peek())) {
            avancar(); // IN_ID -> IN_ID
        }
    
        String lexema = source.substring(inicio, posicao);
        TokenType tipo = ehPalavraReservada(lexema) ? TokenType.PALAVRA_RESERVADA : TokenType.IDENTIFICADOR;
        return new Token(tipo, lexema, l, c);
    }

    private Token scanNumero() {
        int l = linha;
        int c = coluna;
        int inicio = posicao;
        while (hasNext() && ehDigito(peek())) {
            avancar();
        }
        if (peek() == '.') {
            if(ehDigito(lookahead(1))){
                avancar(); // consume '.'
                while (hasNext() && ehDigito(peek())) {
                    avancar();
                }
            } else {
                avancar();
                String lexema = source.substring(inicio, posicao);
                System.err.println("Erro Léxico: número mal formado na linha " + l + ", coluna " + c);
                return new Token(TokenType.ERRO, lexema, l, c);
            }
        } 
        return new Token(TokenType.NUMERO, source.substring(inicio, posicao), l, c);
    }

    private Token scanString() {
        int l = linha;
        int c = coluna;
        avancar(); 
        int inicio = posicao;
        while (hasNext() && peek() != '"' && peek() != '\n') {
            avancar();
        }
        if(peek() == '"') {
            String conteudo = source.substring(inicio, posicao);
            avancar();
            return new Token(TokenType.STRING, conteudo, l, c);
        }
        String parcial = source.substring(inicio, posicao);
        System.err.println("Erro Léxico: string não fechada na linha " + l +", coluna " + c);
        return new Token(TokenType.ERRO, "\"" + parcial, l, c);
    }

    private Token scanOperador() {
        int l = linha;
        int c = coluna;
        char op = avancar();
        char prox = peek();
 
        // == != <= >= && ||
        if ((prox == '=' && (op == '=' || op == '!' || op == '<' || op == '>')) || (op == '&' && prox == '&')
            || (op == '|' && prox == '|')) {
            avancar();
            return new Token(TokenType.OPERADOR, "" + op + prox, l, c);
        }
 
        // '!', '&' e '|'
        if (op == '!' || op == '&' || op == '|') {
            System.err.println("Erro Léxico: operador inválido " + op + " na linha " + l + ", coluna " + c);
            return new Token(TokenType.ERRO, "" + op, l, c);
        }
 
        // = < > + - * /
        return new Token(TokenType.OPERADOR, "" + op, l, c);
    }

    // ----------------------------------------------------------------------------------
    public Token nextToken() {
        pularEspacosEComentarios();
        
        if (!hasNext()) {
            return new Token(TokenType.EOF, "", linha, coluna);
        }

        char x = peek();

        if (ehLetra(x)) {
            return scanReservadaOuIdentificador();
        } else if (ehDigito(x)) {
            return scanNumero();
        } else if (x == '"') {
            return scanString();
        } else if (ehOperador(x)) {
            return scanOperador();
        } else if (ehDelimitador(x)) {
            int l = linha;
            int c = coluna;
            String lexema = "" + avancar();
            return new Token(TokenType.DELIMITADOR, lexema, l, c);
        } else {
            int l = linha;
            int c = coluna;
            String lexema = "" + avancar();
            System.err.println("Erro Léxico: caractere inválido " + lexema + " na linha " + l + ", coluna " + c);
            return new Token(TokenType.ERRO, lexema, l, c);
        }
    }

    public List<Token> tokenizar() {
        List<Token> tokens = new ArrayList<>();
        Token t;
        do {
            t = nextToken();
            tokens.add(t);
        } while (t.tipo != TokenType.EOF);
        return tokens;
    }
}