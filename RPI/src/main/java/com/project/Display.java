package com.project;

public class Display {
    Process p;

    Display( Process p){
        this.p = p;
    }

    public Process getP() {
        return p;
    }

    public void killProces(){
        this.getP().destroy();

        try {
        this.getP().waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
