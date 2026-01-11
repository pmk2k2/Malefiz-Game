package de.hsrm.mi.swtpr.milefiz.model;

public class FigureMoveResult {
    public boolean success;
    public String message;

    public static FigureMoveResult ok() {
        FigureMoveResult r = new FigureMoveResult();
        r.success = true;
        r.message = "OK";
        return r;
    }

    public static FigureMoveResult ok(String msg) {
        FigureMoveResult r = new FigureMoveResult();
        r.success = true;
        r.message = msg;
        return r;
    }

    public static FigureMoveResult fail(String msg) {
        FigureMoveResult r = new FigureMoveResult();
        r.success = false;
        r.message = msg;
        return r;
    }
}


