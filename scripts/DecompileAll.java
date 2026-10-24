// 把 Ghidra 分析结果里所有函数反编译成一份 C 风格文本
// 用法（headless）：analyzeHeadless <proj> <name> -import <bin> -scriptPath scripts -postScript DecompileAll.java
// 输出：环境变量 DECOMP_OUT 指定的路径，默认 /tmp/decompiled.c
import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;

import java.io.FileWriter;
import java.io.PrintWriter;

public class DecompileAll extends GhidraScript {

    @Override
    public void run() throws Exception {
        String out = System.getenv("DECOMP_OUT");
        if (out == null || out.isEmpty()) {
            out = "/tmp/decompiled.c";
        }

        DecompInterface ifc = new DecompInterface();
        ifc.toggleCCode(true);
        ifc.toggleSyntaxTree(true);
        ifc.openProgram(currentProgram);

        PrintWriter w = new PrintWriter(new FileWriter(out));
        w.println("// program : " + currentProgram.getName());
        w.println("// language: " + currentProgram.getLanguageID());
        w.println("// imagebase: " + currentProgram.getImageBase());

        FunctionIterator it = currentProgram.getFunctionManager().getFunctions(true);
        int total = 0;
        int ok = 0;
        while (it.hasNext() && !monitor.isCancelled()) {
            Function f = it.next();
            total++;
            w.println();
            w.println("//================ " + f.getName() + " @ " + f.getEntryPoint() + " ================");
            DecompileResults res = ifc.decompileFunction(f, 60, monitor);
            if (res != null && res.decompileCompleted()) {
                w.println(res.getDecompiledFunction().getC());
                ok++;
            } else {
                w.println("// [decompile failed: "
                        + (res == null ? "null result" : res.getErrorMessage()) + "]");
            }
            if ((total % 100) == 0) {
                println("progress: " + total + " functions, ok=" + ok);
            }
        }
        w.close();
        ifc.dispose();
        println("DONE functions=" + total + " decompiled=" + ok + " -> " + out);
    }
}
