package com.company;

import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Main {

    public static String dataSheetDir = "C:\\Users\\jelmhurst\\Desktop\\Scripts\\Datasheets";
    public ArrayList<RobotData> robotList = new ArrayList<RobotData>();
    public Writer writer = null;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void main(String[] args) {
        new Main().getDataFromDB();

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getDataFromDB() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver Not Registered!");
            e.printStackTrace();
            return;
        }

        System.out.println("Get Data From DB");

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/roborebels","root","roborebels1153");
            Statement stmt = null;
            ResultSet rs = null;



            // create a hash map to store all of the data

            stmt = conn.createStatement();

            // get the match table, create robots and add up number of matches the robot has been in
            rs = stmt.executeQuery("SELECT * from matchtable;");
            // process the stats data
            RobotData rd;
            while(rs.next()) {
                int rn = rs.getInt("RobotNumber");
                String matchName = rs.getString("matchNumber");
                int matchScore = rs.getInt("matchScore");

                if (haveRobot(rn)) {
                    getRobot(rn).matches++;
                    getRobot(rn).totalScore += matchScore;
                    getRobot(rn).avgScore = (float) getRobot(rn).totalScore / getRobot(rn).matches;
                } else {
                    rd = new RobotData();
                    rd.robotNumber = rn;
                    rd.matches = 1;
                    robotList.add(rd);
                }
            }
            rs.close();

            getAvgScoreRank();

            // get the stats table data
            rs = stmt.executeQuery("SELECT * from robotstats;");

            // process the stats data
            while(rs.next()) {

                int rn = rs.getInt("RobotNumber");
                if (haveRobot(rn)) {
                    // get the stats table data
                    getRobot(rn).robotNumber = rs.getInt("RobotNumber");
                    getRobot(rn).moat = rs.getInt("Moat");
                    getRobot(rn).moatAttempt = rs.getInt("Moat");
                    getRobot(rn).lowBar = rs.getInt("LowBar");
                    getRobot(rn).lowBarAttempt = rs.getInt("LowBarAttempt");
                    getRobot(rn).portcullis = rs.getInt("Portcullis");
                    getRobot(rn).portcullisAttempt = rs.getInt("PortcullisAttempt");
                    getRobot(rn).rockWall = rs.getInt("RockWall");
                    getRobot(rn).rockWallAttempt = rs.getInt("RockWallAttempt");
                    getRobot(rn).drawbridge = rs.getInt("Drawbridge");
                    getRobot(rn).drawbridgeAttempt = rs.getInt("DrawbridgeAttempt");
                    getRobot(rn).cheval = rs.getInt("Cheval");
                    getRobot(rn).chevalAttempt = rs.getInt("ChevalAttempt");
                    getRobot(rn).sallyPort = rs.getInt("SallyPort");
                    getRobot(rn).sallyPortAttempt = rs.getInt("SallyPortAttempt");
                    getRobot(rn).ramparts = rs.getInt("Ramparts");
                    getRobot(rn).rampartsAttempt = rs.getInt("RampartsAttempt");
                    getRobot(rn).roughTerrain = rs.getInt("RoughTerrain");
                    getRobot(rn).roughTerrainAttempt = rs.getInt("RoughTerrainAttempt");
                    getRobot(rn).shootHigh = rs.getInt("ShootHigh");
                    getRobot(rn).scoreHigh = rs.getInt("ScoreHigh");
                    getRobot(rn).shootLow = rs.getInt("ShootLow");
                    getRobot(rn).scoreLow = rs.getInt("ScoreLow");
                    getRobot(rn).highAccuracy = rs.getFloat("HighAccuracy");
                    getRobot(rn).lowAccuracy = rs.getFloat("LowAccuracy");
                }

            }

            rs.close();

            // create a new query to pull data from the robot matchdata table
            rs = stmt.executeQuery("SELECT * from matchdata;");
            while(rs.next()) {

                int rn = rs.getInt("RobotNumber");
                if (haveRobot(rn)) {
                    String gameEvent = rs.getString("GameEvent");
                    String subEvent = rs.getString("SubEvent");
                    String phase = rs.getString("phaseOfMatch");

                    if (phase.equals("auto") && subEvent.equals("Cross")) {
                        getRobot(rn).autoCrosses++;
                    }
                    if (gameEvent.equals("Challenge") && subEvent.equals("Succeed")) {
                        getRobot(rn).challenges++;
                    }
                    if (gameEvent.equals("Climb") && subEvent.equals("Succeed")) {
                        getRobot(rn).climbs++;
                    }
                }
            }

            rs.close();

            stmt.close();

            // calculate averages
            for (RobotData r : robotList) {
                getRobot(r.robotNumber).avgHighGoals = (float) getRobot(r.robotNumber).scoreHigh / getRobot(r.robotNumber).matches;
                getRobot(r.robotNumber).avgLowGoals = (float) getRobot(r.robotNumber).scoreLow / getRobot(r.robotNumber).matches;
            }

            // calculate ranks
            getAvgHighGoalScoreRank();
            getAvgLowGoalScoreRank();

            System.out.println("Write HTML Files");

            // loop through all of the robots and write out the data
            for (RobotData r : robotList) {
                // create html file

                try {
                    System.out.println("Robot Number: " + r.robotNumber);

                    String fileName = dataSheetDir + File.separator + r.robotNumber + ".html";
                    File oldFile = new File (fileName);

                    if (oldFile.exists()) {
                        oldFile.delete();
                    }

                    writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(fileName), "utf-8"));

                    writer.write("<!doctype html>\n");

                    createHeader(r.robotNumber);

                    createStyle();

                    writer.write("<body>\n");

                    writer.write("<table>\n");
                    createTableFloatRow("Average Alliance Score",r.avgScore);
                    createTableIntRow("Average Alliance Score Rank",r.avgScoreRank);
                    createTableIntRow("Matches",r.matches);
                    createTableIntRow("Auto Crosses",r.autoCrosses);
                    createTableIntRow("Number of Challenges",r.challenges);
                    createTableIntRow("Number of Climbs",r.climbs);
                    writer.write("</table>\n");

                    // create defense table
                    writer.write("<p><b>Defense Data</b></p>");
                    writer.write("<table>\n");
                    writer.write("<th>Defense</th>\n");
                    writer.write("<th>Crosses</th>\n");
                    writer.write("<th>Attempts</th>\n");
                    writer.write("<th>Avg Crosses/Match</th>\n");
                    createTableDefenseRow("Cheval",r.cheval,r.chevalAttempt, r.matches);
                    createTableDefenseRow("Drawbridge",r.drawbridge,r.drawbridgeAttempt, r.matches);
                    createTableDefenseRow("Low Bar",r.lowBar,r.lowBarAttempt, r.matches);
                    createTableDefenseRow("Moat",r.moat,r.moatAttempt, r.matches);
                    createTableDefenseRow("Ramparts",r.ramparts,r.rampartsAttempt, r.matches);
                    createTableDefenseRow("Rock Wall",r.rockWall,r.rockWallAttempt, r.matches);
                    createTableDefenseRow("Rough Terrain",r.roughTerrain,r.roughTerrainAttempt, r.matches);
                    createTableDefenseRow("Portcullis",r.portcullis,r.portcullisAttempt, r.matches);
                    createTableDefenseRow("Sally Port",r.sallyPort,r.sallyPortAttempt, r.matches);
                    writer.write("</table>\n");

                    // create shooting table
                    writer.write("<p><b>Scoring Data</b></p>");
                    writer.write("<table>\n");
                    writer.write("<th>Target</th>\n");
                    writer.write("<th>Scores</th>\n");
                    writer.write("<th>Attempts</th>\n");
                    writer.write("<th>Avg Goal/Match</th>\n");
                    writer.write("<th>Avg Goal/Match Rank</th>\n");
                    writer.write("<th>Accuracy</th>\n");
                    createTableShootingRow("High Goal",r.scoreHigh,r.shootHigh, r.avgHighGoals, r.avgHighGoalScoreRank, r.highAccuracy);
                    createTableShootingRow("Low Goal",r.scoreLow,r.shootLow, r.avgLowGoals, r.avgLowGoalScoreRank, r.lowAccuracy);

                    writer.write("</body>\n");
                    writer.write("</html>\n");


                    // close html file
                    writer.close();

                } catch (IOException ex) {
                    // report
                }
            }

            // create index.html file
            String fileName = dataSheetDir + File.separator + "index.html";
            File oldFile = new File (fileName);

            if (oldFile.exists()) {
                oldFile.delete();
            }

            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName), "utf-8"));

                writer.write("<!doctype html>\n");
                writer.write("<body>\n");

                // create a list of all robot numbers and sort the list numerically
                ArrayList<Integer> numberList = new ArrayList<Integer>();
                for (RobotData r : robotList) {
                    numberList.add(r.robotNumber);
                }
                Collections.sort(numberList);

                //for (RobotData r : robotList) {

                for (Integer i : numberList) {

                    String url = "." + File.separator + i + ".html";
                    // write links
                    writer.write("<a href=\"" + url + "\">" + i + "</a><br>\n");

                }

                writer.write("</body>\n");
                writer.write("</html>\n");

                // close html file
                writer.close();

            } catch (IOException ex) {
                    // report
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getAvgScoreRank() {
        // rank the robots based on average alliance score
        ArrayList<RobotData> rankList = new ArrayList<RobotData>();
        for (RobotData r : robotList) {
            rankList.add(r);
        }
        Collections.sort(rankList, new Comparator<RobotData>(){
            public int compare(RobotData o1, RobotData o2){
                if(o1.avgScore == o2.avgScore)
                    return 0;
                return o1.avgScore > o2.avgScore ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).avgScoreRank = c + 1;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getAvgHighGoalScoreRank() {
        // rank the robots based on average alliance score
        ArrayList<RobotData> rankList = new ArrayList<RobotData>();
        for (RobotData r : robotList) {
            rankList.add(r);
        }
        Collections.sort(rankList, new Comparator<RobotData>(){
            public int compare(RobotData o1, RobotData o2){
                if(o1.avgHighGoals == o2.avgHighGoals)
                    return 0;
                return o1.avgHighGoals > o2.avgHighGoals ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).avgHighGoalScoreRank = c + 1;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getAvgLowGoalScoreRank() {
        // rank the robots based on average alliance score
        ArrayList<RobotData> rankList = new ArrayList<RobotData>();
        for (RobotData r : robotList) {
            rankList.add(r);
        }
        Collections.sort(rankList, new Comparator<RobotData>(){
            public int compare(RobotData o1, RobotData o2){
                if(o1.avgLowGoals == o2.avgLowGoals)
                    return 0;
                return o1.avgLowGoals > o2.avgLowGoals ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).avgLowGoalScoreRank = c + 1;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createHeader(int robotNumber) {
        String outString = "<html>\n";
        outString += "<title>" + robotNumber + " " + "Robot Stats</title>\n";
        outString += "<h1>" + robotNumber + " " + "Robot Stats</h1>" + "\n";

        try {
            writer.write(outString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createStyle() {
        String outString = "<style>\n";
        outString += "table, th, td {\n";
        outString += "    border: 1px solid black;\n";
        outString += "    border-collapse: collapse;\n";
        outString += "}\n";
        outString += "</style>" + "\n\n";

        try {
            writer.write(outString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createTableFloatRow(String tableText, float tableData) {
        String outString = "<tr>" + "\n";
        outString += "<td>" + tableText + "</td>\n";
        outString += "<td>" + String.format("%.2f", tableData) + "</td>\n";
        outString += "</tr>\n";

        try {
            writer.write(outString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createTableIntRow(String tableText, int tableData) {
        String outString = "<tr>" + "\n";
        outString += "<td>" + tableText + "</td>\n";
        outString += "<td>" + tableData + "</td>\n";
        outString += "</tr>\n";

        try {
            writer.write(outString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createTableDefenseRow(String tableText, int crosses, int attempts, int matches) {
        String outString = "<tr>" + "\n";
        outString += "<td>" + tableText + "</td>\n";
        outString += "<td>" + crosses + "</td>\n";
        outString += "<td>" + attempts + "</td>\n";

        // get average
        float average = (float) crosses / matches;

        outString += "<td>" + String.format("%.2f", average) + "</td>\n";
        outString += "</tr>\n";

        try {
            writer.write(outString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createTableShootingRow(String tableText, int scores, int attempts, float average, int avgRank, float accuracy) {
        String outString = "<tr>" + "\n";
        outString += "<td>" + tableText + "</td>\n";
        outString += "<td>" + scores + "</td>\n";
        outString += "<td>" + attempts + "</td>\n";

        outString += "<td>" + String.format("%.2f", average) + "</td>\n";
        outString += "<td>" + avgRank + "</td>\n";
        outString += "<td>" + String.format("%.2f", accuracy) + "</td>\n";
        outString += "</tr>\n";

        try {
            writer.write(outString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // check the robot list to see if we have a robot already with the given number
    public boolean haveRobot(int robotNumber) {
        boolean doHaveRobot = false;

        for (RobotData r : robotList) {
            if (r.robotNumber == robotNumber) {
                return true;
            }
        }
        return doHaveRobot;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // get the robot with the given robotNumber from the list
    // or return null
    public RobotData getRobot(int robotNumber) {

        for (RobotData r : robotList) {
            if (r.robotNumber == robotNumber) {
                return r;
            }
        }
        return null;
    }
}
