package com.wqy.study.jvm.gc;

/*
* 代码展示两点
* 1：对象可以在GC时自救
* 2：自救机会只有一次，finalize（）最多只会被系统执行调用一次
* */

public class FinalizeEscapeGC {

    public static FinalizeEscapeGC SAVE_HOOK = null;

    public void isAlive() {
        System.out.println("Yes, I am still Alive");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("finalize method executed");
        FinalizeEscapeGC.SAVE_HOOK = this;
    }

    public static void main(String[] args) throws Throwable {
        SAVE_HOOK = new FinalizeEscapeGC();

        //对象第一次自救自己
        SAVE_HOOK = null;
        System.gc();
        //finalize()优先级很低，线程暂停 0.5s， 等待执行
        Thread.sleep(500);
        if (SAVE_HOOK != null) {
            SAVE_HOOK.isAlive();
        } else {
            System.out.println("No, I am dead");
        }

        //代码相同，却不会自救了，因为finalize（）只会被执行一次
        SAVE_HOOK = null;
        System.gc();
        //finalize()优先级很低，线程暂停 0.5s， 等待执行
        Thread.sleep(500);
        if (SAVE_HOOK != null) {
            SAVE_HOOK.isAlive();
        } else {
            System.out.println("No, I am dead 。。。。");
        }
    }
}
