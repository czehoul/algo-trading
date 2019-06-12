package com.yee.trading.auto.funda;

public class Warrants
{
    private String exercise_currency;

    private String maturity_date;

    private String ratio_from;

    private String exercise_ratio;

    private String name;

    private String ratio_to;

    private String code;

    private String type;

    private String exercise_value;

    public String getExercise_currency ()
    {
        return exercise_currency;
    }

    public void setExercise_currency (String exercise_currency)
    {
        this.exercise_currency = exercise_currency;
    }

    public String getMaturity_date ()
    {
        return maturity_date;
    }

    public void setMaturity_date (String maturity_date)
    {
        this.maturity_date = maturity_date;
    }

    public String getRatio_from ()
    {
        return ratio_from;
    }

    public void setRatio_from (String ratio_from)
    {
        this.ratio_from = ratio_from;
    }

    public String getExercise_ratio ()
    {
        return exercise_ratio;
    }

    public void setExercise_ratio (String exercise_ratio)
    {
        this.exercise_ratio = exercise_ratio;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getRatio_to ()
    {
        return ratio_to;
    }

    public void setRatio_to (String ratio_to)
    {
        this.ratio_to = ratio_to;
    }

    public String getCode ()
    {
        return code;
    }

    public void setCode (String code)
    {
        this.code = code;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String getExercise_value ()
    {
        return exercise_value;
    }

    public void setExercise_value (String exercise_value)
    {
        this.exercise_value = exercise_value;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [exercise_currency = "+exercise_currency+", maturity_date = "+maturity_date+", ratio_from = "+ratio_from+", exercise_ratio = "+exercise_ratio+", name = "+name+", ratio_to = "+ratio_to+", code = "+code+", type = "+type+", exercise_value = "+exercise_value+"]";
    }
}