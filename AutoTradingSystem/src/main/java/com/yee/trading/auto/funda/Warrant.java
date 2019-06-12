package com.yee.trading.auto.funda;

public class Warrant
{
    private Warrants warrants;

    

    public Warrants getWarrants ()
    {
        return warrants;
    }

    public void setWarrants (Warrants warrants)
    {
        this.warrants = warrants;
    }

   

    @Override
    public String toString()
    {
        return "ClassPojo [warrants = "+warrants+"]";
    }
}
