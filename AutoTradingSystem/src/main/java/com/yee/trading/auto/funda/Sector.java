package com.yee.trading.auto.funda;

public class Sector
{
    private String description;

    private String name;

    private Board Board;

    private String board_id;

    public String getDescription ()
    {
        return description;
    }

    public void setDescription (String description)
    {
        this.description = description;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public Board getBoard ()
    {
        return Board;
    }

    public void setBoard (Board Board)
    {
        this.Board = Board;
    }

    public String getBoard_id ()
    {
        return board_id;
    }

    public void setBoard_id (String board_id)
    {
        this.board_id = board_id;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [description = "+description+", name = "+name+", Board = "+Board+", board_id = "+board_id+"]";
    }
}
