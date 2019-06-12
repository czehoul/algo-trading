package com.yee.trading.auto.funda;

public class FinancialReport
{
    private String announced_date;

    private String financial_year_end;

    private String DPS;

    private String ref;

    private String quarter_date_end;

    private String EPS;

    private String NTA;

    private String code;

    private String current_quarter;

    private String profit_loss;

    private String ref_name;

    public String getAnnounced_date ()
    {
        return announced_date;
    }

    public void setAnnounced_date (String announced_date)
    {
        this.announced_date = announced_date;
    }

    public String getFinancial_year_end ()
    {
        return financial_year_end;
    }

    public void setFinancial_year_end (String financial_year_end)
    {
        this.financial_year_end = financial_year_end;
    }

    public String getDPS ()
    {
        return DPS;
    }

    public void setDPS (String DPS)
    {
        this.DPS = DPS;
    }

    public String getRef ()
    {
        return ref;
    }

    public void setRef (String ref)
    {
        this.ref = ref;
    }

    public String getQuarter_date_end ()
    {
        return quarter_date_end;
    }

    public void setQuarter_date_end (String quarter_date_end)
    {
        this.quarter_date_end = quarter_date_end;
    }

    public String getEPS ()
    {
        return EPS;
    }

    public void setEPS (String EPS)
    {
        this.EPS = EPS;
    }

    public String getNTA ()
    {
        return NTA;
    }

    public void setNTA (String NTA)
    {
        this.NTA = NTA;
    }

    public String getCode ()
    {
        return code;
    }

    public void setCode (String code)
    {
        this.code = code;
    }

    public String getCurrent_quarter ()
    {
        return current_quarter;
    }

    public void setCurrent_quarter (String current_quarter)
    {
        this.current_quarter = current_quarter;
    }

    public String getProfit_loss ()
    {
        return profit_loss;
    }

    public void setProfit_loss (String profit_loss)
    {
        this.profit_loss = profit_loss;
    }

    public String getRef_name ()
    {
        return ref_name;
    }

    public void setRef_name (String ref_name)
    {
        this.ref_name = ref_name;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [announced_date = "+announced_date+", financial_year_end = "+financial_year_end+", DPS = "+DPS+", ref = "+ref+", quarter_date_end = "+quarter_date_end+", EPS = "+EPS+", NTA = "+NTA+", code = "+code+", current_quarter = "+current_quarter+", profit_loss = "+profit_loss+", ref_name = "+ref_name+"]";
    }
}
