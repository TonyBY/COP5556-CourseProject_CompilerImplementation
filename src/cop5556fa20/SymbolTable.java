package cop5556fa20;

import cop5556fa20.AST.Dec;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

public class SymbolTable {
    static final boolean doPrint = true;
    private void show(Object input) {
        if (doPrint) {
            System.out.println(input.toString());
        }
    }

    Stack<Integer> scope_stack = new Stack<>(); //keeps track of scope number;
    Hashtable<String, ArrayList<Pair>> hash = new Hashtable<String, ArrayList<Pair>>(); // maps identifiers with corresponding scope numbers
    int currentScope, nextScope;

    public SymbolTable() {
        this.scope_stack = new Stack<Integer>();
        this.hash = new Hashtable<String, ArrayList<Pair>>();
        this.currentScope = 0;
        this.nextScope = 1;
        scope_stack.push(currentScope);
    }

    public void enterScope() {
        currentScope = nextScope++;
        scope_stack.push(currentScope);
    }

    public void addDec(String ident, Dec dec) {
        if (hash.containsKey(ident)) {
            ArrayList<Pair> l = hash.get(ident);
            l.add(new Pair(currentScope, dec));
            hash.put(ident, l);
        }else {
            ArrayList<Pair> l = new ArrayList<>();
            l.add(new Pair(currentScope, dec));
            hash.put(ident, l);
        }
    }

    public void closeScope() {
        scope_stack.pop();
        currentScope = scope_stack.peek();
    }

    public Dec lookup(String ident) {
        ArrayList<Pair> l = hash.get(ident);
        if (l == null) {
            return null;
        }

        Dec dec = null;
        int delta = Integer.MAX_VALUE;
        for (Pair p: l) {
            if (p.getKey() <= currentScope && currentScope - p.getKey() <= delta) {
                if (scope_stack.contains(p.getKey())){
                    dec = p.getValue();
                    delta = currentScope - p.getKey();
                }
            }
        }
        return dec;
    }

    public boolean duplicate(String name) {
        ArrayList<Pair> l = hash.get(name);
        if (l == null) {
            show("l == null");
            return false;
        }

        show("currentScope: " + currentScope);
        for (Pair p: l) {
            show("p.getKey(): " + p.getKey());
            if (p.getKey() == currentScope) {
                return true;
            }
        }
        return false;
    }

    class Pair{
        int i;
        Dec d;

        public Pair(int i, Dec d) {
            this.i = i;
            this.d = d;
        }

        public int getKey() {
            return i;
        }

        public Dec getValue() {
            return d;
        }
    }

}
