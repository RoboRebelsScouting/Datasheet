package com.company;

import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class Main {

    public static String dataSheetDir = "";
    public ArrayList<RobotData> robotList = new ArrayList<RobotData>();
    public Writer writer = null;
    public Writer privateWriter = null;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void main(String[] args) {
        // get datasheets directory, check a couple of known directories, if none of those found,
        // create one in Documents
        if (new File("C:\\Users\\Fenton Girls\\Documents\\ROBORBELS1153\\Datasheets").isDirectory()) {
            dataSheetDir = "C:\\Users\\Fenton Girls\\Documents\\ROBORBELS1153\\Datasheets";
        } else {
            dataSheetDir = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Datasheets";
            if (new File(dataSheetDir).isDirectory() == false) {
                // make the directory
                new File(dataSheetDir).mkdir();
            }
        }

        System.out.println("Using datasheet directory: " + dataSheetDir);
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
                    if (phase.equals("auto") && subEvent.equals("Score")) {
                        getRobot(rn).autoScores++;
                    }
                    if (gameEvent.equals("Challenge") && subEvent.equals("Succeed")) {
                        getRobot(rn).challenges++;
                    }
                    if (gameEvent.equals("Climb") && subEvent.equals("Succeed")) {
                        getRobot(rn).climbs++;
                    }

                    if (gameEvent.equals("Brake")||gameEvent.equals("Break")) {
                        getRobot(rn).broken++;
                    }
                }
            }

            rs.close();

            // get the pitinfo data
            rs = stmt.executeQuery("SELECT * from pitinfo;");

            // process the pitinfo data
            while(rs.next()) {

                int rn = rs.getInt("Team");
                if (haveRobot(rn) == false){
                    rd = new RobotData();
                    rd.robotNumber = rn;
                    rd.matches = 1;
                    robotList.add(rd);
                }
                if (haveRobot(rn)) {
                    // get the stats table data
                    String DriveTrainTypeTemp = rs.getString("DriveTrainType");
                    if (DriveTrainTypeTemp.equals("") == false) {
                        getRobot(rn).DriveTrainType = rs.getString("DriveTrainType");
                        getRobot(rn).Climbing = rs.getString("Climbing");
                        getRobot(rn).ShootingStrategy = rs.getString("ShootingStrategy");
                        getRobot(rn).Breach = rs.getString("Breach");
                        getRobot(rn).Shoot = rs.getString("Shoot");
                        getRobot(rn).Defend = rs.getString("Defend");
                        getRobot(rn).Assist = rs.getString("Assist");
                        getRobot(rn).Cheval = rs.getString("Cheval");
                        getRobot(rn).RoughTerrain = rs.getString("RoughTerrain");
                        getRobot(rn).Drawbridge = rs.getString("Drawbridge");
                        getRobot(rn).SallyPort = rs.getString("SallyPort");
                        getRobot(rn).Ramparts = rs.getString("Ramparts");
                        getRobot(rn).Portcullis = rs.getString("Portcullis");
                        getRobot(rn).Rockwall = rs.getString("Rockwall");
                        getRobot(rn).LowBar = rs.getString("LowBar");
                        getRobot(rn).Moat = rs.getString("Moat");
                        getRobot(rn).CoOpInTeam = rs.getString("CoOpInTeam");
                        getRobot(rn).CoOpBtTeam = rs.getString("CoOpBtTeam");
                        getRobot(rn).AutoGetBoulders = rs.getString("AutoGetBoulders");
                        getRobot(rn).AutoCrossDefense = rs.getString("AutoCrossDefense");
                        getRobot(rn).AutoShooting = rs.getString("AutoShooting");
                        getRobot(rn).AutoPosition = rs.getString("AutoPosition");
                        getRobot(rn).AutoBreach = rs.getString("AutoBreach");
                        getRobot(rn).AutoNone = rs.getString("AutoNone");
                        getRobot(rn).Organization = rs.getString("Organization");
                        getRobot(rn).Problems = rs.getString("Problems");
                    }



                } else {
                    rd = new RobotData();
                    rd.robotNumber = rn;
                    rd.matches = 1;
                    robotList.add(rd);
                }
                PitUpdateData pd  = new PitUpdateData();
                pd.Reliability = rs.getString("Reliability");
                pd.ClimberBroke = rs.getString("ClimberBroke");
                pd.DriveTrainBroke = rs.getString("DriveTrainBroke");
                pd.CollectorBroke = rs.getString("CollectorBroke");
                pd.WheelBroke = rs.getString("WheelBroke");
                pd.FrameBroke = rs.getString("FrameBroke");
                pd.ShooterBroke = rs.getString("ShooterBroke");
                pd.AluminumFlaw = rs.getString("AluminumFlaw");
                pd.DirectDriveFlaw = rs.getString("DirectDriveFlaw");
                pd.BatteryFlaw = rs.getString("BatteryFlaw");
                pd.RivetsFlaw = rs.getString("RivetsFlaw");
                pd.SuspensionFlaw = rs.getString("SuspensionFlaw");
                pd.notes = rs.getString("notes");
                System.out.println("Getting Robot Number" + rn);
                String tempString = rs.getString("TimeStampp");
                try{
                    pd.timeStamp = Long.parseLong(tempString);
                }catch (NumberFormatException e){
                    pd.timeStamp = System.currentTimeMillis();
                }
                //pd.timeStamp = rs.getLong("TimeStampp");


                if(dontHavePitData (rn, pd)){
                    getRobot(rn).pitUpdateList.add(pd);
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
            getDefenseRank();

            System.out.println("Write HTML Files");

            // loop through all of the robots and write out the data
            for (RobotData r : robotList) {
                // create html file

                try {
                    System.out.println("Robot Number: " + r.robotNumber);

                    String fileName = dataSheetDir + File.separator + r.robotNumber + ".html";
                    File oldFile = new File (fileName);

                    String privateFileName = dataSheetDir + File.separator + r.robotNumber + "_private.html";
                    File oldPrivateFile = new File (privateFileName);

                    if (oldFile.exists()) {
                        oldFile.delete();
                    }

                    if (oldPrivateFile.exists()) {
                        oldPrivateFile.delete();
                    }

                    writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(fileName), "utf-8"));

                    privateWriter = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(privateFileName), "utf-8"));

                    writeBoth("<!doctype html>\n");

                    createHeader(r.robotNumber);

                    createStyle();

                    writeBoth("<body>\n");
                    writeBoth("<div style=\"clear:both\">\n");
                    writeBoth("<table style=\"float: left;\">\n");
                    createTableFloatRow("Average Alliance Score",r.avgScore);
                    createTableIntRow("Average Alliance Score Rank",r.avgScoreRank);
                    createTableIntRow("Matches",r.matches);
                    createTableIntRow("Auto Crosses",r.autoCrosses);
                    createTableIntRow("Auto Scores", r.autoScores);
                    createTableIntRow("Number of Challenges",r.challenges);
                    createTableIntRow("Number of Climbs",r.climbs);
                    createTableIntRow("Number of Times Robot Broke", r.broken);
                    writeBoth("</table>\n");
                    //photo
                    writeBoth("<div id=\"photo\">\n");
                    writeBoth("<img class=\"border-dotted\" src=\"" + r.robotNumber + ".jpg\" alt=\"Image of "+
                            r.robotNumber +"\" style=\"width: 304px; height: 228px;\">\n");
                    writeBoth("</div>\n");


                    // create defense table
                    writeBoth("<div>\n");
                    writeBoth("<div style=\"float:left;\">\n");
                    writeBoth("<p><b>Defense Data</b></p>");
                    writeBoth("<table>\n");
                    writeBoth("<th>Defense</th>\n");
                    writeBoth("<th>Rank</th>\n");
                    writeBoth("<th>Cross</th>\n");
                    writeBoth("<th>Att</th>\n");
                    writeBoth("<th>%</th>\n");
                    //writeBoth("<th>Avg</th>\n");
                    createTableDefenseRow("Cheval",r.chevalRank, r.cheval,r.chevalAttempt, r.matches);
                    createTableDefenseRow("Drawbridge",r.drawbridgeRank, r.drawbridge,r.drawbridgeAttempt, r.matches);
                    createTableDefenseRow("Low Bar",r.lowBarRank, r.lowBar,r.lowBarAttempt, r.matches);
                    createTableDefenseRow("Moat",r.moatRank, r.moat,r.moatAttempt, r.matches);
                    createTableDefenseRow("Ramparts",r.rampartsRank, r.ramparts,r.rampartsAttempt, r.matches);
                    createTableDefenseRow("Rock Wall",r.rockWallRank, r.rockWall,r.rockWallAttempt, r.matches);
                    createTableDefenseRow("Rough Terrain",r.roughTerrainRank, r.roughTerrain,r.roughTerrainAttempt, r.matches);
                    createTableDefenseRow("Portcullis",r.portcullisRank, r.portcullis,r.portcullisAttempt, r.matches);
                    createTableDefenseRow("Sally Port",r.sallyPortRank, r.sallyPort,r.sallyPortAttempt, r.matches);
                    writeBoth("</table>\n");
                    writeBoth("</div>\n");

                    // create shooting table
                    writeBoth("<div style=\"float:left;\">\n");
                    writeBoth("<p><b>Scoring Data</b></p>");
                    writeBoth("<table>\n");
                    writeBoth("<th>Target</th>\n");
                    writeBoth("<th>Rank</th>\n");
                    writeBoth("<th>Scores</th>\n");
                    writeBoth("<th>Att</th>\n");
                    writeBoth("<th>Avg/Match</th>\n");
                    writeBoth("<th>Accuracy</th>\n");
                    createTableShootingRow("High Goal",r.scoreHigh,r.shootHigh, r.avgHighGoals, r.avgHighGoalScoreRank, r.highAccuracy);
                    createTableShootingRow("Low Goal",r.scoreLow,r.shootLow, r.avgLowGoals, r.avgLowGoalScoreRank, r.lowAccuracy);
                    writeBoth("</table>\n");
                    writeBoth("</div>");
                    writeBoth("</div>");

                    // create pitinfo table
                    privateWriter.write("<div class=\"pitInfo\">");
                    privateWriter.write("<p><b>Pit Info Data</b></p>");
                    privateWriter.write("<table style=\"float: left; width:15%\">\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>DriveTrainType</td>\n");
                    privateWriter.write("<td>" + r.DriveTrainType + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Climbing</td>\n");
                    privateWriter.write("<td>" + r.Climbing + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>ShootingStrategy</td>\n");
                    privateWriter.write("<td>" + r.ShootingStrategy + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Breach</td>\n");
                    privateWriter.write("<td>" + r.Breach + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Shoot</td>\n");
                    privateWriter.write("<td>" + r.Shoot + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Defend</td>\n");
                    privateWriter.write("<td>" + r.Defend + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Assist</td>\n");
                    privateWriter.write("<td>" + r.Assist + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("</table>\n");
                    privateWriter.write("<table style=\"float: left; width:15%\">\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Cheval</td>\n");
                    privateWriter.write("<td>" + r.Cheval + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>RoughTerrain</td>\n");
                    privateWriter.write("<td>" + r.RoughTerrain + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Drawbridge</td>\n");
                    privateWriter.write("<td>" + r.Drawbridge + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>SallyPort</td>\n");
                    privateWriter.write("<td>" + r.SallyPort + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Ramparts</td>\n");
                    privateWriter.write("<td>" + r.Ramparts + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Portcullis</td>\n");
                    privateWriter.write("<td>" + r.Portcullis + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Rockwall</td>\n");
                    privateWriter.write("<td>" + r.Rockwall + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("</table>\n");
                    privateWriter.write("<table style=\"float: left; width:15%\">\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>LowBar</td>\n");
                    privateWriter.write("<td>" + r.LowBar + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Moat</td>\n");
                    privateWriter.write("<td>" + r.Moat + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>AutoGetBoulders</td>\n");
                    privateWriter.write("<td>" + r.AutoGetBoulders + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>AutoCrossDefense</td>\n");
                    privateWriter.write("<td>" + r.AutoCrossDefense + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>AutoShooting</td>\n");
                    privateWriter.write("<td>" + r.AutoShooting + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>AutoPosition</td>\n");
                    privateWriter.write("<td>" + r.AutoPosition + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>AutoBreach</td>\n");
                    privateWriter.write("<td>" + r.AutoBreach + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("</table>\n");
                    privateWriter.write("<table style=\"float: left; width:15%\">\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>AutoNone</td>\n");
                    privateWriter.write("<td>" + r.AutoNone + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Organization</td>\n");
                    privateWriter.write("<td>" + r.Organization + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>CoOpInTeam</td>\n");
                    privateWriter.write("<td>" + r.CoOpInTeam + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>CoOpBtTeam</td>\n");
                    privateWriter.write("<td>" + r.CoOpBtTeam+ "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("<tr>\n");
                    privateWriter.write("<td>Problems</td>\n");
                    privateWriter.write("<td>" + r.Problems + "</td>\n");
                    privateWriter.write("</tr>\n");

                    privateWriter.write("</table>\n");
                    privateWriter.write("</div>\n");


                    //privateWriter.write("<div>\n");
                    privateWriter.write("<div class=\"pitInfo\">");

                    // sort pit updates by date stamp
                    Collections.sort(r.pitUpdateList, new Comparator<PitUpdateData>(){
                        public int compare(PitUpdateData o1, PitUpdateData o2){
                            if(o1.timeStamp == o2.timeStamp)
                                return 0;
                            return o1.timeStamp < o2.timeStamp ? -1 : 1;
                        }
                    });
                    for(PitUpdateData pd : r.pitUpdateList) {

                        privateWriter.write("<p class=\"pitUpdateP\"><b> Update " + convertTime(pd.timeStamp) + "</b></p>\n");

                        // write three tables for pit data true/false data
                        privateWriter.write("<table style=\"float:left\">\n");
                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Climber Broke</td>\n");
                        privateWriter.write("<td>" + pd.ClimberBroke + "</td>\n");
                        privateWriter.write("</tr>\n");

                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Drive Train Broke</td>\n");
                        privateWriter.write("<td>" + pd.DriveTrainBroke + "</td>\n");
                        privateWriter.write("</tr>\n");

                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Collector Broke</td>\n");
                        privateWriter.write("<td>" + pd.CollectorBroke + "</td>\n");
                        privateWriter.write("</tr>\n");

                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Wheel Broke</td>\n");
                        privateWriter.write("<td>" + pd.WheelBroke + "</td>\n");
                        privateWriter.write("</tr>\n");
                        privateWriter.write("</table>\n");

                        privateWriter.write("<table style=\"float:left\">\n");
                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Frame Broke</td>\n");
                        privateWriter.write("<td>" + pd.FrameBroke + "</td>\n");
                        privateWriter.write("</tr>\n");

                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Shooter Broke</td>\n");
                        privateWriter.write("<td>" + pd.ShooterBroke + "</td>\n");
                        privateWriter.write("</tr>\n");

                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Uses Aluminum</td>\n");
                        privateWriter.write("<td>" + pd.AluminumFlaw + "</td>\n");
                        privateWriter.write("</tr>\n");

                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Battery Issues</td>\n");
                        privateWriter.write("<td>" + pd.BatteryFlaw + "</td>\n");
                        privateWriter.write("</tr>\n");
                        privateWriter.write("</table>\n");

                        privateWriter.write("<table style=\"float:left\">\n");
                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Uses Direct Drive</td>\n");
                        privateWriter.write("<td>" + pd.DirectDriveFlaw + "</td>\n");
                        privateWriter.write("</tr>\n");

                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Uses Rivets</td>\n");
                        privateWriter.write("<td>" + pd.RivetsFlaw + "</td>\n");
                        privateWriter.write("</tr>\n");

                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Suspension Problems</td>\n");
                        privateWriter.write("<td>" + pd.SuspensionFlaw + "</td>\n");
                        privateWriter.write("</tr>\n");

                        privateWriter.write("</table>\n");

                        privateWriter.write("<table class=\"pitInfo\">\n");
                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Reliability</td>\n");
                        privateWriter.write("<td style=\"word-wrap: break-word\">" + pd.Reliability + "</td>\n");
                        privateWriter.write("</tr>\n");
                        privateWriter.write("</table>\n");

                        privateWriter.write("<table class=\"pitInfo\">\n");
                        privateWriter.write("<tr>\n");
                        privateWriter.write("<td>Notes</td>\n");
                        privateWriter.write("<td style=\"word-wrap: break-word\">" + pd.notes + "</td>\n");
                        privateWriter.write("</tr>\n");
                        privateWriter.write("</table>\n");
                        privateWriter.write("<br>\n");

                    }

                    privateWriter.write("</div>\n");


                    writeBoth("</body>\n");
                    writeBoth("</html>\n");


                    // close html file
                    writer.close();
                    privateWriter.close();

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

    public boolean dontHavePitData(Integer RobotNumber, PitUpdateData pd){
        for(RobotData r: robotList) {
            if(r.robotNumber == RobotNumber.intValue()){
                for(PitUpdateData p: r.pitUpdateList){
                    if(p.timeStamp == pd.timeStamp){
                        return false;
                    }
                }
            }
        }
        return true;
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

    public void getDefenseRank() {
        // rank the robots based on cheval crosses
        ArrayList<RobotData> rankList = new ArrayList<RobotData>();
        for (RobotData r : robotList) {
            rankList.add(r);
        }
        Collections.sort(rankList, new Comparator<RobotData>(){
            public int compare(RobotData o1, RobotData o2){
                if(o1.cheval == o2.cheval)
                    return 0;
                return o1.cheval > o2.cheval ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).chevalRank = c + 1;
        }
        // get drawbridge rank
        Collections.sort(rankList, new Comparator<RobotData>(){
            public int compare(RobotData o1, RobotData o2){
                if(o1.drawbridge == o2.drawbridge)
                    return 0;
                return o1.drawbridge > o2.drawbridge ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).drawbridgeRank = c + 1;
        }

        //get lowBar rank
        Collections.sort(rankList, new Comparator<RobotData>(){
            public int compare(RobotData o1, RobotData o2){
                if(o1.lowBar == o2.lowBar)
                    return 0;
                return o1.lowBar > o2.lowBar ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).lowBarRank = c + 1;
        }

        //get Moat rank
        Collections.sort(rankList, new Comparator<RobotData>(){
            public int compare(RobotData o1, RobotData o2){
                if(o1.moat == o2.moat)
                    return 0;
                return o1.moat > o2.moat ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).moatRank = c + 1;
        }

        //get portcullis rank
        Collections.sort(rankList, new Comparator<RobotData>(){
            public int compare(RobotData o1, RobotData o2){
                if(o1.portcullis == o2.portcullis)
                    return 0;
                return o1.portcullis > o2.portcullis ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).portcullisRank = c + 1;
        }

        //get ramparts rank
        Collections.sort(rankList, new Comparator<RobotData>(){
            public int compare(RobotData o1, RobotData o2){
                if(o1.ramparts == o2.ramparts)
                    return 0;
                return o1.ramparts > o2.ramparts ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).rampartsRank = c + 1;
        }
        //get rockWall rank
        Collections.sort(rankList, new Comparator<RobotData>(){
            public int compare(RobotData o1, RobotData o2){
                if(o1.rockWall == o2.rockWall)
                    return 0;
                return o1.rockWall > o2. rockWall ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).rockWallRank = c + 1;
        }

        //get roughTerrain rank
        Collections.sort(rankList, new Comparator<RobotData>(){
            public int compare(RobotData o1, RobotData o2){
                if(o1.roughTerrain == o2.roughTerrain)
                    return 0;
                return o1.roughTerrain > o2. roughTerrain ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).roughTerrainRank = c + 1;
        }

        //get sallyPort rank
        Collections.sort(rankList, new Comparator<RobotData>(){
            public int compare(RobotData o1, RobotData o2){
                if(o1.sallyPort == o2.sallyPort)
                    return 0;
                return o1.sallyPort > o2.sallyPort ? -1 : 1;
            }
        });
        // now loop through the lists and set the rank based on avg score
        for (int c = 0; c < rankList.size(); c++) {
            getRobot(rankList.get(c).robotNumber).sallyPortRank = c + 1;
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
        outString += "<h1 style=\"align:left;float:left\">" + robotNumber + " " + "Robot Stats</h1>" + "\n";
        outString += "<p style=\"align:left;float:left\"><small>(" + convertTime(System.currentTimeMillis()) + ")</small></p>" + "\n";

        //try {
        writeBoth(outString);
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createStyle() {
        String outString = "<style>\n";
        outString += "h1 {\n";
        outString += "     font-family: verdana;\n";
        outString += "     font-size: 125%;\n";
        outString += "}\n";
        outString += "p   {\n";
        outString += "    font-family: verdana;\n";
        outString += "    font-size: 100%\n";
        outString += "}\n";
        outString += ".pitUpdateP   {\n";
        outString += "    font-family: verdana;\n";
        outString += "    font-size: 80%\n";
        outString += "}\n";
        outString += "table, th, td {\n";
        outString += "    font-family: verdana;\n";
        outString += "    border: 1px solid black;\n";
        outString += "    border-collapse: collapse;\n";
        outString += "    padding:2px;\n";
        outString += "    margin-right:10px;\n";
        outString += "    font-size: 80%;\n";
        outString += "}\n";
        outString += ".border-dotted {\n";
        outString += "    border: 2px dotted #9799a7:\n";
        outString += "}\n";
        outString += ".pitInfo {\n";
        outString += "clear: both;\n";
        outString += "margin-top:20px;\n";
        outString += "padding: 20px 0px 0px 0px;\n";
        outString += "}\n";
        outString += "</style>" + "\n\n";

        //try {
        writeBoth(outString);
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createTableFloatRow(String tableText, float tableData) {
        String outString = "<tr>" + "\n";
        outString += "<td>" + tableText + "</td>\n";
        outString += "<td>" + String.format("%.2f", tableData) + "</td>\n";
        outString += "</tr>\n";

        //try {
        writeBoth(outString);
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createTableIntRow(String tableText, int tableData) {
        String outString = "<tr>" + "\n";
        outString += "<td>" + tableText + "</td>\n";
        outString += "<td>" + tableData + "</td>\n";
        outString += "</tr>\n";

        //try {
        writeBoth(outString);
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createTableDefenseRow(String tableText, int rank, int crosses, int attempts, int matches) {
        String outString = "<tr>" + "\n";
        outString += "<td>" + tableText + "</td>\n";
        outString += "<td>" + rank + "</td>\n";
        outString += "<td>" + crosses + "</td>\n";
        outString += "<td>" + attempts + "</td>\n";
        // get average
        float average = (float) crosses / matches;
        float percentage = (float) crosses / attempts * 100;
        if(attempts==0) {
            percentage= 0.0f;
        }
        outString += "<td>" + String.format("%.2f", percentage) + "</td>\n";
        //outString += "<td>" + String.format("%.2f", average) + "</td>\n";
        outString += "</tr>\n";

        //try {
        writeBoth(outString);
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void createTableShootingRow(String tableText, int scores, int attempts, float average, int avgRank, float accuracy) {
        String outString = "<tr>" + "\n";
        outString += "<td>" + tableText + "</td>\n";
        outString += "<td>" + avgRank + "</td>\n";
        outString += "<td>" + scores + "</td>\n";
        outString += "<td>" + attempts + "</td>\n";

        outString += "<td>" + String.format("%.2f", average) + "</td>\n";
        outString += "<td>" + String.format("%.2f", accuracy) + "</td>\n";
        outString += "</tr>\n";

        //try {
        writeBoth(outString);
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
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
    public String convertTime(long time){
        Date date = new Date(time);
        Format format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        return format.format(date); }

    public void writeBoth(String StringToWrite){
        try {
            writer.write(StringToWrite);
            privateWriter.write(StringToWrite);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
