/**
 * This code was developed for the class project in COP5556 Programming Language Principles
 * at the University of Florida, Fall 2020.
 *
 * This software is solely for the educational benefit of students
 * enrolled in the course during the Fall 2020 semester.
 *
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 *
 *  @Beverly A. Sanders, 2020
 *
 */

package cop5556fa20;

import cop5556fa20.AST.Type;
import cop5556fa20.AST.*;
import cop5556fa20.runtime.BufferedImageUtils;
import cop5556fa20.runtime.LoggedIO;
import cop5556fa20.runtime.PLPImage;
import cop5556fa20.runtime.PixelOps;
import org.objectweb.asm.*;

import java.util.List;

import static cop5556fa20.Scanner.Kind;

public class CodeGenVisitorComplete implements ASTVisitor, Opcodes {
    static final boolean doPrint = true;
    private void show(Object input) {
        if (doPrint) {
            System.out.println(input.toString());
        }
    }

    final String className;
    final boolean isInterface = false;
    ClassWriter cw;
    MethodVisitor mv;

    private int arg_slot = 0;
    private int slot = 1;  // initialize slot number for local variables

    public CodeGenVisitorComplete(String className) {
        super();
        this.className = className;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//		cw = new ClassWriter(0); //If the call to methodVisitor.visitMaxs crashes, it
        // is
        // sometime helpful to
        // temporarily run it without COMPUTE_FRAMES. You
        // won't get a completely correct classfile, but
        // you will be able to see the code that was
        // generated.

        // String sourceFileName = className; //TODO Temporary solution, FIX THIS
        int version = -65478;
        cw.visit(version, ACC_PUBLIC | ACC_SUPER, className, null, "java/lang/Object", null);
        cw.visitSource(null, null);
        // create main method
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        // initialize
        mv.visitCode();
        // insert label before first instruction
        Label mainStart = new Label();

        FieldVisitor fieldVisitorX = cw.visitField(ACC_STATIC, "X", "I", null, null);
        fieldVisitorX.visitEnd();
        FieldVisitor fieldVisitorY = cw.visitField(ACC_STATIC, "Y", "I", null, null);
        fieldVisitorY.visitEnd();

        mv.visitLabel(mainStart);
        // visit children to add instructions to method
        List<ASTNode> nodes = program.decOrStatement();
        for (ASTNode node : nodes) {
            if (node.isDec) {
                show("node.isDec");
                Dec dec = (Dec) node;
                dec.slot = slot;
                dec.visit(this, arg);
                slot++;
            } else {
                node.visit(this, null);
            }
        }
        // add  required (by the JVM) return statement to main
        mv.visitInsn(RETURN);

        // adds label at end of code
        Label mainEnd = new Label();
        mv.visitLabel(mainEnd);
        // handles parameters and local variables of main. The only local var is args
        mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
        // Sets max stack size and number of local vars.
        // Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
        // asm will calculate this itself and the parameters are ignored.
        // If you have trouble with failures in this routine, it may be useful
        // to temporarily set the parameter in the ClassWriter constructor to 0.
        // The generated classfile will not pass verification, but you will at least be
        // able to see what instructions it contains.
        mv.visitMaxs(0, 0);

        // finish construction of main method
        mv.visitEnd();

        // finish class construction
        cw.visitEnd();

        // generate classfile as byte array and return
        return cw.toByteArray();

    }

    /**
     * Add a static field to the class for this variable.
     */
    @Override
    public Object visitDecVar(DecVar decVar, Object arg) throws Exception {
        String varName = decVar.name();
        Type type = decVar.type();
        String desc;

        if (type == Type.String) {desc = "Ljava/lang/String;";}
        else if (type == Type.Int) {desc = "I";}
        else {throw new UnsupportedOperationException("visitDecVar can only be type of Sting or Int. Received: " + type);}

        FieldVisitor fieldVisitor = cw.visitField(ACC_STATIC, varName, desc, null, null);
        fieldVisitor.visitEnd();

        //evaluate initial value and store in variable, if one is given.
        Expression e = decVar.expression();
        if (e != Expression.empty) {
            e.visit(this, null); // generates code to evaluate expression and leave value on top of the stack
//			mv.visitLdcInsn(e.visit(this, null));

            mv.visitFieldInsn(PUTSTATIC, className, varName, desc);
        }
        return null;
    }

    @Override
    public Object visitDecImage(DecImage decImage, Object arg) throws Exception {
        // TODO Auto-generated method stub
        String imgName = decImage.name();
        Expression e0 = decImage.width();
        Expression e1 = decImage.height();
        Expression e2 = decImage.source();
        Kind op = decImage.op();
        Type type = decImage.type();
        Type type0 = e0.type();
        Type type1 = e1.type();
        Type type2 = e2.type();
        String desc = "Lcop5556fa20/runtime/PLPImage;";

        FieldVisitor fieldVisitor = cw.visitField(ACC_STATIC, imgName, desc, null, null);
        fieldVisitor.visitEnd();

        mv.visitTypeInsn(NEW, PLPImage.className);
        mv.visitInsn(DUP);
//        mv.visitInsn(ACONST_NULL);
//        mv.visitInsn(ACONST_NULL);
//        mv.visitMethodInsn(INVOKESPECIAL, PLPImage.className, "<init>", "(Ljava/awt/image/BufferedImage;Ljava/awt/Dimension;)V", false);
//        mv.visitVarInsn(ASTORE, decImage.slot);
//        mv.visitVarInsn(ALOAD, decImage.slot);

//        mv.visitFieldInsn(PUTSTATIC, className, imgName, desc);

//        mv.visitFieldInsn(PUTFIELD, PLPImage.className, "declaredSize", "Ljava/awt/Dimension;");

        switch (op) {
            case LARROW -> {
                e2.visit(this, null);
                if (type2 == Type.String) {
                    mv.visitMethodInsn(INVOKESTATIC, BufferedImageUtils.className, "fetchBufferedImage", "(Ljava/lang/String;)Ljava/awt/image/BufferedImage;", false);
                    if (e0 != Expression.empty) {
                        e0.visit(this, null);
                        e1.visit(this, null);
                        mv.visitMethodInsn(INVOKESTATIC, BufferedImageUtils.className, "resizeBufferedImage", "(Ljava/awt/image/BufferedImage;II)Ljava/awt/image/BufferedImage;", false);
                    }
                }
                if (type2 == Type.Image) {
                    mv.visitFieldInsn(GETFIELD, PLPImage.className, "image", "Ljava/awt/image/BufferedImage;");
                    mv.visitMethodInsn(INVOKESTATIC, BufferedImageUtils.className, "copyBufferedImage", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
                    if (e0 != Expression.empty) {
                        e0.visit(this, null);
                        e1.visit(this, null);
                        mv.visitMethodInsn(INVOKESTATIC, BufferedImageUtils.className, "resizeBufferedImage", "(Ljava/awt/image/BufferedImage;II)Ljava/awt/image/BufferedImage;", false);
                    }
                }
            }
            case ASSIGN -> {
                Label setTrue_width = new Label();
                Label setTrue_height = new Label();
                Label endEB = new Label();

                Label setTrue_img = new Label();

                int line = e2.first().line();
                int posInLine = e2.first().posInLine();
                String message = "Cannot assign the image. Size doesn't match.";
                String message_imgNull = "Cannot assign the image. RHS.image cannot be null.";

                if (e0 != Expression.empty) {
                    e2.visit(this, null);
                    mv.visitLdcInsn(line);
                    mv.visitLdcInsn(posInLine);
                    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getWidthThrows", PLPImage.getWidthThrowsSig, false);
                    e0.visit(this, null);
                    mv.visitJumpInsn(IF_ICMPEQ, setTrue_width);
                    mv.visitLdcInsn(line);
                    mv.visitLdcInsn(posInLine);
                    mv.visitLdcInsn(message);
                    mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, "throwPLPImageException", PLPImage.throwPLPImageExceptionSig, false);

                    mv.visitLabel(setTrue_width);
                    e2.visit(this, null);
                    mv.visitLdcInsn(line);
                    mv.visitLdcInsn(posInLine);
                    mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getHeightThrows", PLPImage.getHeightThrowsSig, false);
                    e1.visit(this, null);
                    mv.visitJumpInsn(IF_ICMPEQ, setTrue_height);
                    mv.visitLdcInsn(line);
                    mv.visitLdcInsn(posInLine);
                    mv.visitLdcInsn(message);
                    mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, "throwPLPImageException", PLPImage.throwPLPImageExceptionSig, false);
                    mv.visitLabel(setTrue_height);
                    mv.visitLabel(endEB);
                }
                e2.visit(this, null);
                mv.visitFieldInsn(GETFIELD, PLPImage.className, "image", "Ljava/awt/image/BufferedImage;");
                mv.visitJumpInsn(IFNONNULL, setTrue_img);
                mv.visitLdcInsn(line);
                mv.visitLdcInsn(posInLine);
                mv.visitLdcInsn(message_imgNull);
                mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, "throwPLPImageException", PLPImage.throwPLPImageExceptionSig, false);

                mv.visitLabel(setTrue_img);
                e2.visit(this, null);
                mv.visitFieldInsn(GETFIELD, PLPImage.className, "image", "Ljava/awt/image/BufferedImage;");
            }
            default -> mv.visitInsn(ACONST_NULL);
        }

        if (e0 != Expression.empty && e1 != Expression.empty) {
            show("e0 != Empty");
            mv.visitTypeInsn(NEW, "java/awt/Dimension");
            mv.visitInsn(DUP);
            e0.visit(this, null);
            e1.visit(this, null);
            mv.visitMethodInsn(INVOKESPECIAL, "java/awt/Dimension", "<init>", "(II)V", false);
//            print the top 1 element in the stack
//            mv.visitInsn(DUP); //duplicat the top value so we only work on the copy
//            mv.visitFieldInsn(GETSTATIC,"java/lang/System", "out", "Ljava/io/PrintStream;");//put System.out to operand stack
//            mv.visitInsn(SWAP); // swap of the top two values of the opestack: value1 value2 => value2 value1
//            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");

            show("Initialized a java/awt/Dimension instance.");
        }
        else{
            mv.visitInsn(ACONST_NULL);
        }

//        print the top 2 element in the stack
//        mv.visitInsn(DUP2);
//
//        //print the top values and remove from the stack
//        mv.visitFieldInsn(GETSTATIC,"java/lang/System", "out", "Ljava/io/PrintStream;");//put System.out to operand stack
//        mv.visitInsn(SWAP);
//        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");
//
//        //print the second value and remove from the stack
//        mv.visitFieldInsn(GETSTATIC,"java/lang/System", "out", "Ljava/io/PrintStream;");//put System.out to operand stack
//        mv.visitInsn(SWAP);
//        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V");

        mv.visitMethodInsn(INVOKESPECIAL, PLPImage.className, "<init>", "(Ljava/awt/image/BufferedImage;Ljava/awt/Dimension;)V", false);
        //        mv.visitVarInsn(ASTORE, decImage.slot);
        mv.visitFieldInsn(PUTSTATIC, className, imgName, desc);
        return null;

//        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
        // TODO Auto-generated method stub
        String name = statementAssign.name();
        Expression e = statementAssign.expression();
        Dec dec = statementAssign.dec();
        Type type = dec.type();
        String desc;

        switch (type) {
            case String -> {
                desc = "Ljava/lang/String;";
            }
            case Int -> {
                //IMPLEMENT THIS FOR ASSIGNMENT 5
                desc = "I";
//				throw new UnsupportedOperationException("not yet implemented");
            }
            case Image -> {
                desc = "Lcop5556fa20/runtime/PLPImage;";
            }
            default -> throw new UnsupportedOperationException("not yet implemented");
        }

        if (type == Type.String || type== Type.Int){
            if (e != Expression.empty) {
                mv.visitFieldInsn(GETSTATIC, className, name, desc);
                e.visit(this, null); // generates code to evaluate expression and leave value on top of the stack
                mv.visitFieldInsn(PUTSTATIC, className, name, desc);
            }
        } else { // type == Type.Image
            Label setTrue_dim = new Label();
            Label setTrue_ExprImage = new Label();
            Label setTrue_width = new Label();
            Label setTrue_height = new Label();
            Label endSA = new Label();
            int line = e.first().line();
            int posInLine = e.first().posInLine();
            String message = "Cannot assign the image. Size doesn't match.";
            String message_noImageToAssign = "RHS Image field is null.";

            e.visit(this, null);
            mv.visitFieldInsn(GETFIELD, PLPImage.className, "image", "Ljava/awt/image/BufferedImage;");

            mv.visitJumpInsn(IFNONNULL, setTrue_ExprImage);
            mv.visitLdcInsn(line);
            mv.visitLdcInsn(posInLine);
            mv.visitLdcInsn(message_noImageToAssign);
            mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, "throwPLPImageException", PLPImage.throwPLPImageExceptionSig, false);

            mv.visitLabel(setTrue_ExprImage);
            mv.visitFieldInsn(GETSTATIC, className, name, desc);
            mv.visitFieldInsn(GETFIELD, PLPImage.className, "declaredSize", "Ljava/awt/Dimension;");
            mv.visitJumpInsn(IFNONNULL, setTrue_dim);
            mv.visitJumpInsn(GOTO, endSA);

            mv.visitLabel(setTrue_dim);
            mv.visitFieldInsn(GETSTATIC, className, name, desc);
            mv.visitLdcInsn(line);
            mv.visitLdcInsn(posInLine);
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getWidthThrows", PLPImage.getWidthThrowsSig, false);
            e.visit(this, null);
            mv.visitLdcInsn(line);
            mv.visitLdcInsn(posInLine);
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getWidthThrows", PLPImage.getWidthThrowsSig, false);

            mv.visitJumpInsn(IF_ICMPEQ, setTrue_width);
            mv.visitLdcInsn(line);
            mv.visitLdcInsn(posInLine);
            mv.visitLdcInsn(message);
            mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, "throwPLPImageException", PLPImage.throwPLPImageExceptionSig, false);

            mv.visitLabel(setTrue_width);
            mv.visitFieldInsn(GETSTATIC, className, name, desc);
            mv.visitLdcInsn(line);
            mv.visitLdcInsn(posInLine);
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getHeightThrows", PLPImage.getHeightThrowsSig, false);
            e.visit(this, null);
            mv.visitLdcInsn(line);
            mv.visitLdcInsn(posInLine);
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getHeightThrows", PLPImage.getHeightThrowsSig, false);

            mv.visitJumpInsn(IF_ICMPEQ, setTrue_height);
            mv.visitLdcInsn(line);
            mv.visitLdcInsn(posInLine);
            mv.visitLdcInsn(message);
            mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, "throwPLPImageException", PLPImage.throwPLPImageExceptionSig, false);

            mv.visitLabel(setTrue_height);
            mv.visitLabel(endSA);

            mv.visitFieldInsn(GETSTATIC, className, name, desc);
            e.visit(this, null);
            mv.visitFieldInsn(GETFIELD, PLPImage.className, "image", "Ljava/awt/image/BufferedImage;");
            mv.visitFieldInsn(PUTFIELD, PLPImage.className, // the class
                    "image", // the field name
                    "Ljava/awt/image/BufferedImage;"//the field type
            );
        }
        return null;
//		throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Object visitStatementImageIn(StatementImageIn statementImageIn, Object arg) throws Exception {
        String name = statementImageIn.name();
        Expression e0 = statementImageIn.source();
        Type type0 = e0.type();
        String desc = "Lcop5556fa20/runtime/PLPImage;";

        Label setTrue_dim = new Label();
        Label setTrue_width = new Label();
        Label setTrue_height = new Label();
        Label endResize = new Label();
        int line = e0.first().line();
        int posInLine = e0.first().posInLine();
        String message = "Cannot assign the image. Size doesn't match.";
        String message_noImageToAssign = "RHS Image field is null.";
        if (type0 == Type.String) {
            mv.visitFieldInsn(GETSTATIC, className, name, desc);
            e0.visit(this, null);
            mv.visitMethodInsn(INVOKESTATIC, BufferedImageUtils.className, "fetchBufferedImage", "(Ljava/lang/String;)Ljava/awt/image/BufferedImage;", false);

            mv.visitFieldInsn(GETSTATIC, className, name, desc);
            mv.visitFieldInsn(GETFIELD, PLPImage.className, "declaredSize", "Ljava/awt/Dimension;");
            mv.visitJumpInsn(IFNONNULL, setTrue_dim);
            mv.visitJumpInsn(GOTO, endResize);

            mv.visitLabel(setTrue_dim);
//            e0.visit(this, null);
//            mv.visitMethodInsn(INVOKESTATIC, BufferedImageUtils.className, "fetchBufferedImage", "(Ljava/lang/String;)Ljava/awt/image/BufferedImage;", false);
            mv.visitFieldInsn(GETSTATIC, className, name, desc);
            mv.visitLdcInsn(line);
            mv.visitLdcInsn(posInLine);
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getWidthThrows", PLPImage.getWidthThrowsSig, false);
            mv.visitFieldInsn(GETSTATIC, className, name, desc);
            mv.visitLdcInsn(line);
            mv.visitLdcInsn(posInLine);
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getHeightThrows", PLPImage.getHeightThrowsSig, false);
            mv.visitMethodInsn(INVOKESTATIC, BufferedImageUtils.className, "resizeBufferedImage", "(Ljava/awt/image/BufferedImage;II)Ljava/awt/image/BufferedImage;", false);

            mv.visitLabel(endResize);
            mv.visitFieldInsn(PUTFIELD, PLPImage.className, // the class
                    "image", // the field name
                    "Ljava/awt/image/BufferedImage;"//the field type
            );
        } else if (type0 == Type.Image) {
            mv.visitFieldInsn(GETSTATIC, className, name, desc);
            e0.visit(this, null);
            mv.visitFieldInsn(GETFIELD, PLPImage.className, "image", "Ljava/awt/image/BufferedImage;");
            mv.visitMethodInsn(INVOKESTATIC, BufferedImageUtils.className, "copyBufferedImage", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);

            mv.visitFieldInsn(GETSTATIC, className, name, desc);
            mv.visitFieldInsn(GETFIELD, PLPImage.className, "declaredSize", "Ljava/awt/Dimension;");
            mv.visitJumpInsn(IFNONNULL, setTrue_dim);
            mv.visitJumpInsn(GOTO, endResize);

            mv.visitLabel(setTrue_dim);
//            e0.visit(this, null);
//            mv.visitFieldInsn(GETFIELD, PLPImage.className, "image", "Ljava/awt/image/BufferedImage;");
//            mv.visitMethodInsn(INVOKESTATIC, BufferedImageUtils.className, "copyBufferedImage", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
            mv.visitFieldInsn(GETSTATIC, className, name, desc);
            mv.visitLdcInsn(line);
            mv.visitLdcInsn(posInLine);
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getWidthThrows", PLPImage.getWidthThrowsSig, false);
            mv.visitFieldInsn(GETSTATIC, className, name, desc);
            mv.visitLdcInsn(line);
            mv.visitLdcInsn(posInLine);
            mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getHeightThrows", PLPImage.getHeightThrowsSig, false);
            mv.visitMethodInsn(INVOKESTATIC, BufferedImageUtils.className, "resizeBufferedImage", "(Ljava/awt/image/BufferedImage;II)Ljava/awt/image/BufferedImage;", false);

            mv.visitLabel(endResize);
            mv.visitFieldInsn(PUTFIELD, PLPImage.className, // the class
                    "image", // the field name
                    "Ljava/awt/image/BufferedImage;"//the field type
            );
        }
        return null;
    }

    @Override
    public Object visitStatementLoop(StatementLoop statementLoop, Object arg) throws Exception {
        String name = statementLoop.name();
        Expression e0 = statementLoop.cond();
        Expression e1 = statementLoop.e();
        String desc = "Lcop5556fa20/runtime/PLPImage;";

        int nameLine = statementLoop.first().line();
        int namePosInLine = statementLoop.first().posInLine();
        String nameMessage = "Image has not been initialized.";
        Label endCheck = new Label();

        Label startCond = new Label();
        Label createImage = new Label();
        Label updatePixel = new Label();
        Label nextPixel = new Label();
        Label newColumn = new Label();
        Label endLoop = new Label();

        // Check if the image has been initialized or not.
        mv.visitFieldInsn(GETSTATIC, className, name, desc);
        mv.visitFieldInsn(GETFIELD, PLPImage.className, "image", "Ljava/awt/image/BufferedImage;");
        mv.visitJumpInsn(IFNONNULL, endCheck);

        mv.visitFieldInsn(GETSTATIC, className, name, desc);
        mv.visitFieldInsn(GETFIELD, PLPImage.className, "declaredSize", "Ljava/awt/Dimension;");
        mv.visitJumpInsn(IFNONNULL, createImage);
        mv.visitLdcInsn(nameLine);
        mv.visitLdcInsn(namePosInLine);
        mv.visitLdcInsn(nameMessage);
        mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, "throwPLPImageException", PLPImage.throwPLPImageExceptionSig, false);

        // Label: createImage
        mv.visitLabel(createImage);
        mv.visitFieldInsn(GETSTATIC, className, name, desc);

        //Get Width.
        mv.visitFieldInsn(GETSTATIC, className, name, desc);
        mv.visitLdcInsn(nameLine);
        mv.visitLdcInsn(namePosInLine);
        mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getWidthThrows", PLPImage.getWidthThrowsSig, false);
        // Get height.
        mv.visitFieldInsn(GETSTATIC, className, name, desc);
        mv.visitLdcInsn(nameLine);
        mv.visitLdcInsn(namePosInLine);
        mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getHeightThrows", PLPImage.getHeightThrowsSig, false);

        mv.visitMethodInsn(INVOKESTATIC, BufferedImageUtils.className, "createBufferedImage", "(II)Ljava/awt/image/BufferedImage;", false);
        mv.visitFieldInsn(PUTFIELD, PLPImage.className, // the class
                "image", // the field name
                "Ljava/awt/image/BufferedImage;"//the field type
        );

        mv.visitLabel(endCheck);

        //Init X, Y
        mv.visitLdcInsn(0);
        mv.visitFieldInsn(PUTSTATIC, className, "X", "I");
        mv.visitLdcInsn(0);
        mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");

        //Label: startCond. Eval Condition: e0
        mv.visitLabel(startCond);
        if (e0 != Expression.empty) {
            e0.visit(this, null);
            mv.visitLdcInsn(true);
            mv.visitJumpInsn(IF_ICMPEQ, updatePixel);
            mv.visitJumpInsn(GOTO, nextPixel);
        }

        // Label: updatePixel
        mv.visitLabel(updatePixel);
        mv.visitFieldInsn(GETSTATIC, className, name, desc);
        mv.visitFieldInsn(GETSTATIC, className, "X", "I");
        mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
        e1.visit(this, null);
        mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "updatePixel", PLPImage.updatePixelSig, false);
        mv.visitJumpInsn(GOTO, nextPixel);

        // Label: nextPixel. move cursor
        mv.visitLabel(nextPixel);
        // Check if finish scanning a column of the image.
        mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
        mv.visitLdcInsn(1);
        mv.visitInsn(IADD);
        // Get height.
        mv.visitFieldInsn(GETSTATIC, className, name, desc);
        mv.visitLdcInsn(nameLine);
        mv.visitLdcInsn(namePosInLine);
        mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getHeightThrows", PLPImage.getHeightThrowsSig, false);

        mv.visitJumpInsn(IF_ICMPEQ, newColumn);
        mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
        mv.visitLdcInsn(1);
        mv.visitInsn(IADD);
        mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");
        mv.visitJumpInsn(GOTO, startCond);

        //Label: newColumn
        mv.visitLabel(newColumn);
        mv.visitLdcInsn(0);
        mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");

        //Check if finish scanning the whole image.
        mv.visitFieldInsn(GETSTATIC, className, "X", "I");
        mv.visitLdcInsn(1);
        mv.visitInsn(IADD);
        //Get Width.
        mv.visitFieldInsn(GETSTATIC, className, name, desc);
        mv.visitLdcInsn(nameLine);
        mv.visitLdcInsn(namePosInLine);
        mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getWidthThrows", PLPImage.getWidthThrowsSig, false);

        mv.visitJumpInsn(IF_ICMPEQ, endLoop);
        mv.visitFieldInsn(GETSTATIC, className, "X", "I");
        mv.visitLdcInsn(1);
        mv.visitInsn(IADD);
        mv.visitFieldInsn(PUTSTATIC, className, "X", "I");
        mv.visitJumpInsn(GOTO, startCond);

        mv.visitLabel(endLoop);

        return null;
//        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Object visitStatementOutFile(StatementOutFile statementOutFile, Object arg) throws Exception {
        String name = statementOutFile.name();
        Expression e0 = statementOutFile.filename();
        String desc = "Lcop5556fa20/runtime/PLPImage;";

        mv.visitFieldInsn(GETSTATIC, className, name, desc);
        show("file out!");
//                mv.visitVarInsn(ALOAD, dec.slot);
        e0.visit(this, null);
        mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "imageToFile", LoggedIO.imageToFileSig,
                isInterface);

        return null;
    }

    @Override
    public Object visitStatementOutScreen(StatementOutScreen statementOutScreen, Object arg) throws Exception {
        String name = statementOutScreen.name();
        Expression e0 = statementOutScreen.X();
        Expression e1 = statementOutScreen.Y();
        Type type0 = e0.type();
        Type type1 = e1.type();

        Dec dec = statementOutScreen.dec();
        Type type = dec.type();
        String desc;
        switch (type) {
            case String -> {
                desc = "Ljava/lang/String;";

                mv.visitFieldInsn(GETSTATIC, className, name, desc);
                mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "stringToScreen", LoggedIO.stringToScreenSig,
                        isInterface);
                //mv.visitJumpInsn();
            }
            case Int -> {
                //IMPLEMENT THIS FOR ASSIGNMENT 5
                desc = "I";

                mv.visitFieldInsn(GETSTATIC, className, name, desc);
                mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "intToScreen", LoggedIO.intToScreenSig,
                        isInterface);
//				throw new UnsupportedOperationException("not yet implemented");
            }
            case Image -> {
                desc = PLPImage.desc;
                mv.visitFieldInsn(GETSTATIC, className, name, desc);
                show("screen out!");
//                mv.visitVarInsn(ALOAD, dec.slot);
                mv.visitVarInsn(BIPUSH, 0);
                mv.visitVarInsn(BIPUSH, 0);
                mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "imageToScreen", LoggedIO.imageToScreenSig,
                        isInterface);
//                throw new UnsupportedOperationException("not yet implemented");
            }
            default -> throw new UnsupportedOperationException("not yet implemented");
        }


        return null;
    }

    @Override
    public Object visitExprArg(ExprArg exprArg, Object arg) throws Exception {
        // TODO Auto-generated method stub
        Type type = exprArg.type();
        Expression e = exprArg.e();
        mv.visitVarInsn(ALOAD, arg_slot); // arg_slot = 0
        e.visit(this, arg);
        mv.visitInsn(AALOAD);

        if (type == Type.Int) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I",
                    isInterface);
        }

//		String desc;
//		switch (type) {
//			case String -> {
//				desc = "Ljava/lang/String;";
//
//				mv.visitFieldInsn(GETSTATIC, className, name, desc);
//				//mv.visitJumpInsn();
//			}
//			case Int -> {
//				//IMPLEMENT THIS FOR ASSIGNMENT 5
//				desc = "I";
//
//				mv.visitFieldInsn(GETSTATIC, className, name, desc);
//				mv.visitMethodInsn(INVOKESTATIC, LoggedIO.className, "intToScreen", LoggedIO.intToScreenSig,
//						isInterface);
////				throw new UnsupportedOperationException("not yet implemented");
//			}
//			default -> throw new UnsupportedOperationException("not yet implemented");
//		}

        return null;
//		throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Object visitExprConditional(ExprConditional exprConditional, Object arg) throws Exception {
        // TODO Auto-generated method stub
        Expression eCon = exprConditional.condition();
        Expression eTrue = exprConditional.trueCase();
        Expression eFalse =  exprConditional.falseCase();

        Label start = new Label();
        Label startTrue = new Label();
        Label startFalse = new Label();
        Label end = new Label();

        mv.visitLabel(start);
        eCon.visit(this, arg);
        mv.visitJumpInsn(IFNE, startTrue);

        mv.visitLabel(startFalse);
        eFalse.visit(this, arg);

        Label endFalse = new Label();
        mv.visitLabel(endFalse);
        mv.visitJumpInsn(GOTO, end); //GOTO END

        mv.visitLabel(startTrue);
        eTrue.visit(this, arg);
        Label endTrue = new Label();
        mv.visitLabel(endTrue);

        mv.visitLabel(end);
        return null;
//		throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Object visitExprBinary(ExprBinary exprBinary, Object arg) throws Exception {
        // TODO Auto-generated method stub
        Type type = exprBinary.type();
        Kind op = exprBinary.op();
        Expression e0 = exprBinary.e0();
        Expression e1 = exprBinary.e1();
        Type type0 = e0.type();
        Type type1 = e1.type();

        Label setTrue = new Label();
        Label endEB = new Label();

        if (type0 == Type.Int) {
            switch (op) {
                case PLUS -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitInsn(IADD);
                }
                case MINUS -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitInsn(ISUB);
                }
                case STAR -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitInsn(IMUL);
                }
                case DIV -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitInsn(IDIV);
                }
                case MOD -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitInsn(IREM);
                }
                case EQ -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitJumpInsn(IF_ICMPEQ, setTrue);
                    mv.visitLdcInsn(false);
                }
                case NEQ -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitJumpInsn(IF_ICMPNE, setTrue);
                    mv.visitLdcInsn(false);
                }
                case GE -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitJumpInsn(IF_ICMPGE, setTrue);
                    mv.visitLdcInsn(false);
                }
                case GT -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitJumpInsn(IF_ICMPGT, setTrue);
                    mv.visitLdcInsn(false);
                }
                case LE -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitJumpInsn(IF_ICMPLE, setTrue);
                    mv.visitLdcInsn(false);
                }
                case LT -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitJumpInsn(IF_ICMPLT, setTrue);
                    mv.visitLdcInsn(false);
                }
                default -> throw new UnsupportedOperationException("not yet implemented");
            }
            mv.visitJumpInsn(GOTO, endEB);
            mv.visitLabel(setTrue);
            mv.visitLdcInsn(true);
            mv.visitLabel(endEB);

        } else if (type0 == Type.Boolean) {
            switch (op) {
                case AND -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitInsn(IAND);
                }
                case OR -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitInsn(IOR);
                }
                case EQ -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitJumpInsn(IF_ICMPEQ, setTrue);
                    mv.visitLdcInsn(false);
                }
                case NEQ -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitJumpInsn(IF_ICMPNE, setTrue);
                    mv.visitLdcInsn(false);
                }

                default-> throw new UnsupportedOperationException("not yet implemented");
            }
            mv.visitJumpInsn(GOTO, endEB);
            mv.visitLabel(setTrue);
            mv.visitLdcInsn(true);
            mv.visitLabel(endEB);
        } else if (type0 == Type.String) {
            switch (op) {
                case PLUS -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
//					mv.visitInsn(IAND);
                    mv.visitMethodInsn(INVOKESTATIC, CodeGenUtils.className, "stringConcatenation",
                            "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false);
                }
                case EQ -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitMethodInsn(INVOKESTATIC, CodeGenUtils.className, "stringComparison",
                            "(Ljava/lang/String;Ljava/lang/String;)Z", false);
                    mv.visitLdcInsn(true);
                    mv.visitJumpInsn(IF_ICMPEQ, setTrue);
//                    mv.visitJumpInsn(IF_ACMPEQ, setTrue);
                    mv.visitLdcInsn(false);
                }
                case NEQ -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitJumpInsn(IF_ACMPNE, setTrue);
                    mv.visitLdcInsn(false);
                }

                default-> throw new UnsupportedOperationException("not yet implemented");
            }
            mv.visitJumpInsn(GOTO, endEB);
            mv.visitLabel(setTrue);
            mv.visitLdcInsn(true);
            mv.visitLabel(endEB);
        } else if (type0 == Type.Image) {
            switch (op) {
                case EQ -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitMethodInsn(INVOKESTATIC, CodeGenUtils.className, "imageComparison",
                            "(Lcop5556fa20/runtime/PLPImage;Lcop5556fa20/runtime/PLPImage;)Z", false);
                    mv.visitLdcInsn(true);
                    mv.visitJumpInsn(IF_ICMPEQ, setTrue);
                    mv.visitLdcInsn(false);
                }
                case NEQ -> {
                    e0.visit(this, arg);
                    e1.visit(this, arg);
                    mv.visitMethodInsn(INVOKESTATIC, CodeGenUtils.className, "imageComparison",
                            "(Lcop5556fa20/runtime/PLPImage;Lcop5556fa20/runtime/PLPImage;)Z", false);
                    mv.visitLdcInsn(true);
                    mv.visitJumpInsn(IF_ICMPNE, setTrue);
                    mv.visitLdcInsn(false);
                }

                default-> throw new UnsupportedOperationException("not yet implemented");
            }
            mv.visitJumpInsn(GOTO, endEB);
            mv.visitLabel(setTrue);
            mv.visitLdcInsn(true);
            mv.visitLabel(endEB);
        }

        return null;
//		throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Object visitExprUnary(ExprUnary exprUnary, Object arg) throws Exception {
        // TODO Auto-generated method stub
        Kind op = exprUnary.op();

        Expression e = exprUnary.e();

        if (e != Expression.empty) {
            e.visit(this, arg); // generates code to evaluate expression and leave value on top of the stack
        }

        switch (op) {
            case PLUS -> {
//				int val1 = (Integer) e.visit(this, null);
//				e.setVal(val1);
//				mv.visitLdcInsn(e.val());
            }
            case MINUS -> {
//				int val1 = (Integer) e.visit(this, null);
//				e.setVal(-1 * val1);
//				mv.visitLdcInsn(e.val());

                mv.visitLdcInsn(-1);
                mv.visitInsn(IMUL);
            }
            case EXCL -> {
//				boolean val1 = (Boolean) e.visit(this,null);
//				e.setVal(val1); //update attribute

                Label originalT = new Label();
                Label originalF = new Label();

                mv.visitLdcInsn(true);
                mv.visitJumpInsn(IF_ICMPEQ, originalT);
                mv.visitLdcInsn(true);
                mv.visitJumpInsn(GOTO, originalF);
                mv.visitLabel(originalT);
                mv.visitLdcInsn(false);
                mv.visitLabel(originalF);
            }
            default -> throw new UnsupportedOperationException("not yet implemented");
        }

//		return e.val();
        return null;
//		throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Object visitExprIntLit(ExprIntLit exprIntLit, Object arg) throws Exception {
        // TODO Auto-generated method stub
        show("exprIntLit.value(): " + exprIntLit.value());
        mv.visitLdcInsn(exprIntLit.value());
        return exprIntLit.value();
//		throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Object visitExprVar(ExprVar exprVar, Object arg) throws Exception {
        // TODO Auto-generated method stub
        String name = exprVar.name();
        Type type = exprVar.type();

        if (name == "X") {
            mv.visitFieldInsn(GETSTATIC, className, "X", "I");
            return null;
        } else if (name == "Y") {
            mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
            return null;
        }

        String desc;
        switch (type) {
            case String -> {
                desc = "Ljava/lang/String;";
            }
            case Int -> {
                //IMPLEMENT THIS FOR ASSIGNMENT 5
                desc = "I";
//				throw new UnsupportedOperationException("not yet implemented");
            }
            case Image -> {
                desc = PLPImage.desc;
//                mv.visitVarInsn(ALOAD, dec.slot);
//                return null;
            }
            default -> throw new UnsupportedOperationException("not yet implemented");
        }

        mv.visitFieldInsn(GETSTATIC, className, name, desc);
        return null;

//		throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * generate code to put the value of the StringLit on the stack.
     */
    @Override
    public Object visitExprStringLit(ExprStringLit exprStringLit, Object arg) throws Exception {

//		exprStringLit.setVal(exprStringLit.text());
//		return exprStringLit.val();
        mv.visitLdcInsn(exprStringLit.text());
        return null;
    }

    @Override
    public Object visitExprConst(ExprConst exprConst, Object arg) throws Exception {
        // TODO Auto-generated method stub
//		exprConst.setVal(exprConst.value());
//		return exprConst.value();
        mv.visitLdcInsn(exprConst.value());
        return null;
//		throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Object visitExprHash(ExprHash exprHash, Object arg) throws Exception {
        String attr = exprHash.attr();
        Expression e0 = exprHash.e();
        Type type0 = e0.type();
        int line = e0.first().line();
        int posInLine = e0.first().posInLine();
        String message = "Image has not been initialized.";
        Label endCheck = new Label();

        if (type0 == Type.Image){
            // Check if the image has been initialized or not.
            e0.visit(this, null);
            mv.visitFieldInsn(GETFIELD, PLPImage.className, "image", "Ljava/awt/image/BufferedImage;");
            mv.visitJumpInsn(IFNONNULL, endCheck);

            e0.visit(this, null);
            mv.visitFieldInsn(GETFIELD, PLPImage.className, "declaredSize", "Ljava/awt/Dimension;");
            mv.visitJumpInsn(IFNONNULL, endCheck);
            mv.visitLdcInsn(line);
            mv.visitLdcInsn(posInLine);
            mv.visitLdcInsn(message);
            mv.visitMethodInsn(INVOKESTATIC, PLPImage.className, "throwPLPImageException", PLPImage.throwPLPImageExceptionSig, false);
        }

        mv.visitLabel(endCheck);
        switch (attr){
            case "width" -> {
                e0.visit(this, null);
                mv.visitLdcInsn(line);
                mv.visitLdcInsn(posInLine);
                mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getWidthThrows", PLPImage.getWidthThrowsSig, false);
            }
            case "height" -> {
                e0.visit(this, null);
                mv.visitLdcInsn(line);
                mv.visitLdcInsn(posInLine);
                mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "getHeightThrows", PLPImage.getHeightThrowsSig, false);
            }
            case "red" -> {
                e0.visit(this, null);
                mv.visitMethodInsn(INVOKESTATIC, PixelOps.className, "getRed", PixelOps.getRedSig, false);
            }
            case "green" -> {
                e0.visit(this, null);
                mv.visitMethodInsn(INVOKESTATIC, PixelOps.className, "getGreen", PixelOps.getGreenSig, false);
            }
            case "blue" -> {
                e0.visit(this, null);
                mv.visitMethodInsn(INVOKESTATIC, PixelOps.className, "getBlue", PixelOps.getBlueSig, false);
            }
            default -> {throw new UnsupportedOperationException("not yet implemented");}
        }

        return null;
//        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Object visitExprPixelConstructor(ExprPixelConstructor exprPixelConstructor, Object arg) throws Exception {
        Expression e0 = exprPixelConstructor.redExpr();
        Expression e1 = exprPixelConstructor.greenExpr();
        Expression e2 = exprPixelConstructor.blueExpr();

        e0.visit(this, null);
        e1.visit(this, null);
        e2.visit(this, null);
        mv.visitMethodInsn(INVOKESTATIC, PixelOps.className, "makePixel", PixelOps.makePixelSig, false);
        return null;
//        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Object visitExprPixelSelector(ExprPixelSelector exprPixelSelector, Object arg) throws Exception {
        Expression e0 = exprPixelSelector.image();
        Expression e1 = exprPixelSelector.X();
        Expression e2 = exprPixelSelector.Y();

        e0.visit(this, null);
        e1.visit(this, null);
        e2.visit(this, null);
        mv.visitMethodInsn(INVOKEVIRTUAL, PLPImage.className, "selectPixel", PLPImage.selectPixelSig, false);
        return null;
//        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Object visitExprEmpty(ExprEmpty exprEmpty, Object arg) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }
}
