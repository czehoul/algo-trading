package com.yee.trading.auto.funda;

public class Entitlement
{
    private String amount;

    private String DPS;

    private String id;

    private String indicator;

    private String payment_date;

    private String date_announced;

    private String entitlement_subject;

    private String ref_url;

    private String code;

    private String ex_date;

    private String entitlement_date;

    public String getAmount ()
    {
        return amount;
    }

    public void setAmount (String amount)
    {
        this.amount = amount;
    }

    public String getDPS ()
    {
        return DPS;
    }

    public void setDPS (String DPS)
    {
        this.DPS = DPS;
    }

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public String getIndicator ()
    {
        return indicator;
    }

    public void setIndicator (String indicator)
    {
        this.indicator = indicator;
    }

    public String getPayment_date ()
    {
        return payment_date;
    }

    public void setPayment_date (String payment_date)
    {
        this.payment_date = payment_date;
    }

    public String getDate_announced ()
    {
        return date_announced;
    }

    public void setDate_announced (String date_announced)
    {
        this.date_announced = date_announced;
    }

    public String getEntitlement_subject ()
    {
        return entitlement_subject;
    }

    public void setEntitlement_subject (String entitlement_subject)
    {
        this.entitlement_subject = entitlement_subject;
    }

    public String getRef_url ()
    {
        return ref_url;
    }

    public void setRef_url (String ref_url)
    {
        this.ref_url = ref_url;
    }

    public String getCode ()
    {
        return code;
    }

    public void setCode (String code)
    {
        this.code = code;
    }

    public String getEx_date ()
    {
        return ex_date;
    }

    public void setEx_date (String ex_date)
    {
        this.ex_date = ex_date;
    }

    public String getEntitlement_date ()
    {
        return entitlement_date;
    }

    public void setEntitlement_date (String entitlement_date)
    {
        this.entitlement_date = entitlement_date;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [amount = "+amount+", DPS = "+DPS+", id = "+id+", indicator = "+indicator+", payment_date = "+payment_date+", date_announced = "+date_announced+", entitlement_subject = "+entitlement_subject+", ref_url = "+ref_url+", code = "+code+", ex_date = "+ex_date+", entitlement_date = "+entitlement_date+"]";
    }
}

