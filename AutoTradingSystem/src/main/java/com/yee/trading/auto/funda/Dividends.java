package com.yee.trading.auto.funda;

public class Dividends
{
    private Entitlement Entitlement;

    public Entitlement getEntitlement ()
    {
        return Entitlement;
    }

    public void setEntitlement (Entitlement Entitlement)
    {
        this.Entitlement = Entitlement;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Entitlement = "+Entitlement+"]";
    }
}