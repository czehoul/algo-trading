package com.yee.trading.auto.funda;

public class StockFundaDetails
{
	private Warrant Warrant;
	
    private Dividends[] Dividends;

    private Sector Sector;

    private Stock Stock;

    private FinancialReport[] FinancialReport;

    public Warrant getWarrant ()
    {
        return Warrant;
    }

    public void setWarrant (Warrant Warrant)
    {
        this.Warrant = Warrant;
    }
    
    public Dividends[] getDividends ()
    {
        return Dividends;
    }

    public void setDividends (Dividends[] Dividends)
    {
        this.Dividends = Dividends;
    }

    public Sector getSector ()
    {
        return Sector;
    }

    public void setSector (Sector Sector)
    {
        this.Sector = Sector;
    }

    public Stock getStock ()
    {
        return Stock;
    }

    public void setStock (Stock Stock)
    {
        this.Stock = Stock;
    }

    public FinancialReport[] getFinancialReport ()
    {
        return FinancialReport;
    }

    public void setFinancialReport (FinancialReport[] FinancialReport)
    {
        this.FinancialReport = FinancialReport;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Warrant = "+Warrant+", Dividends = "+Dividends+", Sector = "+Sector+", Stock = "+Stock+", FinancialReport = "+FinancialReport+"]";
    }
}
