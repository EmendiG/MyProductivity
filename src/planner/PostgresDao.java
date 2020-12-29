package planner;

import planner.functionalities.CalendarDate;
import planner.task.AllTimeTasks;
import planner.task.NewTasks;
import planner.task.Tasks;
import planner.task.ThisMonthTasks;

import java.sql.*;
import java.util.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PostgresDao {
    public static final String DB_NAME = "myproductivitydb";
    public static final String CONNECTION_STRING = "jdbc:postgresql://localhost:5432/" + DB_NAME;
    public static final String DB_USER = "planner";
    public static final String DB_PASS = "planner";


    public static final String TABLE_TASKS = "TASKS";
    public static final String COLUMN_TASK_ID = "TASK_ID";
    public static final String COLUMN_START_TIME = "START_TIME";
    public static final String COLUMN_END_TIME = "END_TIME";

    public static final String TABLE_TASKNAMES = "TASKNAMES";
    public static final String COLUMN_TASK_NAME = "TASK_NAME";

    public static final String TABLE_TASKGOALS = "TASKGOALS";
    public static final String COLUMN_GOAL_CHOICE = "GOAL_CHOICE";
    public static final String COLUMN_GOAL_HOURS = "GOAL_HOURS";
    public static final String COLUMN_GOAL_DATE = "GOAL_DATE";


    public static final String CREATE_TABLE_TASKS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TASKS +
            " (ID                   SERIAL          PRIMARY KEY     NOT NULL, " +
            COLUMN_TASK_ID      +"  INT                             NOT NULL, " +
            COLUMN_START_TIME   +"  TIMESTAMP                       NOT NULL, " +
            COLUMN_END_TIME     +"  TIMESTAMP                                )";

    public static final String CREATE_TABLE_TASKNAMES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TASKNAMES +
            " ("+COLUMN_TASK_ID +"  SERIAL          PRIMARY KEY     NOT NULL, " +
            COLUMN_TASK_NAME    +"  VARCHAR (60)                            )";

    public static final String CREATE_TABLE_GOALS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TASKGOALS +
            " ("+COLUMN_TASK_ID +"  INT             PRIMARY KEY     NOT NULL, " +
            COLUMN_GOAL_CHOICE  +"  VARCHAR (20)                    NOT NULL, " +
            COLUMN_GOAL_HOURS   +"  INT                                     , " +
                    COLUMN_GOAL_DATE +"  TIMESTAMP                                )";



    public static final String INSERT_TASK =
            "INSERT INTO " + TABLE_TASKS +
                    "(" + COLUMN_TASK_ID + "," +  COLUMN_START_TIME + ")" +
            " VALUES (                 ?    ,                     ?    )" ;

    public static final String UPDATE_END_TIME =
            "UPDATE "   + TABLE_TASKS +
            " SET "      + COLUMN_END_TIME + "=?" +
            " WHERE id=?";

    public static final String QUERY_TASK_DURATION_IN_TIME_RANGE =
            "SELECT " + TABLE_TASKNAMES +"."+   COLUMN_TASK_ID      + ", "  +
                        TABLE_TASKS     +"."+   COLUMN_START_TIME   + ", "  +
                        TABLE_TASKS     +"."+   COLUMN_END_TIME     + ", "  +
                        TABLE_TASKNAMES +"."+   COLUMN_TASK_NAME    + ", "  +
                        TABLE_TASKGOALS +"."+   COLUMN_GOAL_CHOICE  + ", "  +
                        TABLE_TASKGOALS +"."+   COLUMN_GOAL_HOURS   + ", "  +
                        TABLE_TASKGOALS +"."+ COLUMN_GOAL_DATE +
            " FROM " + TABLE_TASKS +

            " FULL OUTER JOIN " + TABLE_TASKNAMES +
                " ON "+ TABLE_TASKS     +"."+   COLUMN_TASK_ID      + "="   +
                        TABLE_TASKNAMES +"."+   COLUMN_TASK_ID              +

            " FULL OUTER JOIN " + TABLE_TASKGOALS +
                " ON "+ TABLE_TASKNAMES +"."+   COLUMN_TASK_ID      + "="   +
                        TABLE_TASKGOALS +"."+   COLUMN_TASK_ID;

    public static final String CONDITIONS_TIME_RANGE_NO_NULL =
            " AND "     + TABLE_TASKS    +"."+   COLUMN_START_TIME   + " >= '%1$tF %1$tT' " +
            " AND "     + TABLE_TASKS    +"."+   COLUMN_END_TIME     + " <= '%2$tF %2$tT' ";

    public static final String CONDITIONS_TIME_RANGE_WITH_NULL =
            " WHERE "   + TABLE_TASKS    +"."+   COLUMN_START_TIME   + " >= '%1$tF %1$tT' " +
            " AND "     + TABLE_TASKS    +"."+   COLUMN_END_TIME     + " <= '%2$tF %2$tT' " + // "{2,date,yyyy-MM-dd hh:mm:ss}"
            " OR  "     + TABLE_TASKS    +"."+   COLUMN_START_TIME   + " IS NULL "          +
            " OR  "     + TABLE_TASKS    +"."+   COLUMN_END_TIME     + " IS NULL ";

    public static final String QUERY_TASKNAMES =
            "SELECT * FROM " + TABLE_TASKNAMES;

    public static final String INSERT_TASKNAME =
            "INSERT INTO " + TABLE_TASKNAMES +
                    "(" + COLUMN_TASK_NAME + ")" +
            " VALUES (                   ?    )" ;

    public static final String INSERT_GOAL =
            "INSERT INTO " + TABLE_TASKGOALS +
                    "( " + COLUMN_TASK_ID + "," +  COLUMN_GOAL_CHOICE    + "," + COLUMN_GOAL_HOURS   + "," + COLUMN_GOAL_DATE + " )" +
            " VALUES (                  ?    ,                      ?       ,                    ?      ,                     ?     )" ;

    public static final String DELETE_NULLS_FROM_TABLE_TASKS =
            "DELETE FROM " + TABLE_TASKS +
            " WHERE " + COLUMN_END_TIME + " IS NULL";

    public static final String UPDATE_LAST_NULL_FROM_TABLE_TASKS =
            "UPDATE " + TABLE_TASKS +
            " SET " + COLUMN_END_TIME + " = ? " +
            " WHERE ID IN( SELECT max(id) FROM " + TABLE_TASKS + " )" +
            " AND " + COLUMN_END_TIME + " IS NULL";


    private PreparedStatement insertTask;
    private PreparedStatement updateEndTime;
    private PreparedStatement insertNewTaskname;
    private PreparedStatement insertGoal;
    private PreparedStatement updateLastNullInTasks;

    private Connection conn;
    private Statement stmt;

    // not thread safe
    private static final PostgresDao instance = new PostgresDao();
    private PostgresDao(){}
    public static PostgresDao getInstance() {
        return instance;
    }


    public boolean open() {
        try {
            conn = DriverManager.getConnection(CONNECTION_STRING, DB_USER, DB_PASS);
            insertTask = conn.prepareStatement(INSERT_TASK, Statement.RETURN_GENERATED_KEYS);
            updateEndTime = conn.prepareStatement(UPDATE_END_TIME);
            insertNewTaskname = conn.prepareStatement(INSERT_TASKNAME, Statement.RETURN_GENERATED_KEYS);
            insertGoal = conn.prepareStatement(INSERT_GOAL);
            updateLastNullInTasks = conn.prepareStatement(UPDATE_LAST_NULL_FROM_TABLE_TASKS);
            return true;

        } catch (SQLException e) {
            System.out.println("Couldn't connect to database: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if(insertTask != null)
                insertTask.close();
            if(updateEndTime != null)
                updateEndTime.close();
            if(insertNewTaskname != null)
                insertNewTaskname.close();
            if(insertGoal != null)
                insertGoal.close();
            if(updateLastNullInTasks != null)
                updateLastNullInTasks.close();

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Couldn't close connection: " + e.getMessage());
        }
    }

    public void setUpTable(String sql) throws SQLException {
        Connection conn = DriverManager.getConnection(CONNECTION_STRING, DB_USER, DB_PASS);
        stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
        conn.close();
    }


    /**
     ::getSortedTaskByLengthInTimeRange::

     taskID = 0 to query ALL TASKS !!!

    */
    public List<AllTimeTasks> getSortedTaskByLengthInTimeRange(int taskID, Timestamp startTime, Timestamp endTime, int dayOffset, String dayOfTheWeek) {
        StringBuilder sb = new StringBuilder(QUERY_TASK_DURATION_IN_TIME_RANGE);
        // taskIDs are starting from 1
        if (taskID == 0)
            sb.append(String.format(CONDITIONS_TIME_RANGE_WITH_NULL, startTime, endTime));
        else {
            sb.append(" WHERE " + TABLE_TASKNAMES + "." + COLUMN_TASK_ID + " = ");          // no need to query particular task and then add nulls
            sb.append(taskID);
            sb.append(String.format(CONDITIONS_TIME_RANGE_NO_NULL, startTime, endTime ));
        }

        try (Connection conn = DriverManager.getConnection(CONNECTION_STRING, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {

            // calendar to check task duration performed today / in the past
            Calendar calEnd = new CalendarDate(dayOffset).getOffsettedCalendar();
            List<AllTimeTasks> tasks = new ArrayList<>();
            ResultSet rs = stmt.executeQuery(sb.toString());

            while (rs.next()) {
                AllTimeTasks task = new AllTimeTasks();

                task.setTaskName(rs.getString(COLUMN_TASK_NAME));
                task.setTaskID(rs.getInt(COLUMN_TASK_ID));
                task.setGoalChoice(rs.getString(COLUMN_GOAL_CHOICE));
                if ( rs.getInt(COLUMN_GOAL_HOURS) != 0 )
                    task.setGoalDuration(rs.getInt(COLUMN_GOAL_HOURS));
                if ( rs.getTimestamp(COLUMN_GOAL_DATE) != null )
                    task.setGoalDate(rs.getTimestamp(COLUMN_GOAL_DATE).getTime() );
                if ( rs.getTimestamp(COLUMN_END_TIME) != null && rs.getTimestamp(COLUMN_START_TIME) != null ) {
                    task.setTaskDuration(rs.getTimestamp(COLUMN_END_TIME).getTime() - rs.getTimestamp(COLUMN_START_TIME).getTime());
                    // check if this task was performed today and for how long, AND how long this task was performed in the past
                    if ( rs.getTimestamp(COLUMN_END_TIME).getTime() < calEnd.getTimeInMillis() )
                        task.setTaskDoneTillThisPeriod( rs.getTimestamp(COLUMN_END_TIME).getTime() - rs.getTimestamp(COLUMN_START_TIME).getTime() );
                    else
                        task.setTaskDoneThisPeriod( rs.getTimestamp(COLUMN_END_TIME).getTime() - rs.getTimestamp(COLUMN_START_TIME).getTime() );
                    // map days to whole day task duration in order to show it on graphs
                    LocalDate mapKey = rs.getTimestamp(COLUMN_START_TIME).toLocalDateTime().toLocalDate();
                    if (dayOfTheWeek != null && ! dayOfTheWeek.equals(Weekdays.EVERYDAY.toString()) ) {
                        if (mapKey.getDayOfWeek().equals(DayOfWeek.valueOf(dayOfTheWeek))) {
                            task.putHowLongTaskDoneADay(mapKey,
                                    (rs.getTimestamp(COLUMN_END_TIME).getTime()
                                            - rs.getTimestamp(COLUMN_START_TIME).getTime()));
                        }
                    } else {
                        task.putHowLongTaskDoneADay(mapKey,
                                (rs.getTimestamp(COLUMN_END_TIME).getTime()
                                        - rs.getTimestamp(COLUMN_START_TIME).getTime()));
                    }
                }
                else
                    task.setTaskDuration(0L);
                tasks.add(task);
            }

            Map<Integer, AllTimeTasks> mappedTasks =
                tasks.stream().collect(Collectors.toMap(ThisMonthTasks::getTaskID, Function.identity(), ThisMonthTasks::merge));
            List<AllTimeTasks> sortedTasks = new ArrayList<>(mappedTasks.values());
            // set collected task duration from a period assign it as a time string
            sortedTasks.forEach(x -> x.setTaskDurationInString( x.getTaskDoneThisPeriod() ));
            // tasks firstly go to right ListView and taskDuration is the most important factor there
            sortedTasks.sort((x,y) -> x.getTaskDuration() > y.getTaskDuration() ? -1 : 1);
            return sortedTasks;

        } catch (SQLException e) {
            System.out.println("SQL statement that has failed: " + "\n\t" + sb.toString() + "\n");
            System.out.println("Sorted task query failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<String> getListedTasknames() {
        try (Connection conn = DriverManager.getConnection(CONNECTION_STRING, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(QUERY_TASKNAMES);
            List<String> tasknames = new ArrayList<>();
            while (rs.next())
                tasknames.add(rs.getString(COLUMN_TASK_NAME));
            return tasknames;

        } catch (SQLException e) {
            System.out.println("Get listed " + COLUMN_TASK_NAME + " query has failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public long insertNewTaskname(String taskname) {
        long newTaskID = Long.MAX_VALUE;
        try {
            insertNewTaskname.setString(1, taskname);
            insertNewTaskname.executeUpdate();
            ResultSet rs= insertNewTaskname.getGeneratedKeys();
            if (rs.next())
                newTaskID = rs.getLong(1);
            return newTaskID;

        } catch (SQLException e) {
            System.out.println("New " + COLUMN_TASK_NAME + " insertion has failed: " + e.getMessage());
        }
        return newTaskID;
    }

    public void insertNewGoal(NewTasks newTask) {
        int taskID = (int) insertNewTaskname(newTask.getNewTaskName().trim());

        try {
            insertGoal.setInt(1, taskID);
            insertGoal.setString(2, newTask.getGoalChoice());
            // goalHours (accuracy to 0.5) * 60 are always int, no rest is left:
            if (newTask.getGoalHours() != null && newTask.getGoalHours() != 0.0)
                insertGoal.setInt(3, (int) (newTask.getGoalHours() * 1000*60*60));
            else
                insertGoal.setNull(3, java.sql.Types.NULL);

            if (newTask.getPickedDate() != null)
                insertGoal.setTimestamp(4,  Timestamp.valueOf(newTask.getPickedDate().atStartOfDay()) );
            else
                insertGoal.setNull(4, java.sql.Types.NULL);

            insertGoal.executeUpdate();
        } catch (SQLException e) {
            System.out.println("New goal insertion has failed: " + e.getMessage());
        }

    }

    public Tasks insertTask(Tasks task) throws SQLException {
        insertTask.setInt(1,task.getTaskID());
        insertTask.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        insertTask.executeUpdate();
        ResultSet rs= insertTask.getGeneratedKeys();
        if (rs.next())
            task.setID(rs.getLong(1));
        return task;
    }

    public void insertEndTime(long ID) throws SQLException {
        updateEndTime.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        updateEndTime.setLong(2, ID);
        updateEndTime.executeUpdate();
    }

    public void deleteTasksWithoutEndTime() {
        try (Connection conn = DriverManager.getConnection(CONNECTION_STRING, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement() ) {
            stmt.executeUpdate(DELETE_NULLS_FROM_TABLE_TASKS);
        } catch (SQLException e) {
            System.out.println("Couldn't delete nulls from " + TABLE_TASKS + " table " + e.getMessage());
        }
    }

    public void updateEndTimeForLastTaskIfNull() {
        try {
            updateLastNullInTasks.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            updateLastNullInTasks.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Couldn't delete nulls from " + TABLE_TASKS + " table " + e.getMessage());
        }
    }

}

