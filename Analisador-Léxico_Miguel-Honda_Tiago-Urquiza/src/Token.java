public class Token {
public final TokenType tipo;
public final String lexema;
public final int linha, coluna;

public Token(TokenType tipo, String lexema, int linha, int coluna) {
    this.tipo = tipo;
    this.lexema = lexema;
    this.linha = linha;
    this.coluna = coluna;
}}
