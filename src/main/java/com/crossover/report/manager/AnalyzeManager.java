package com.crossover.report.manager;

import com.crossover.report.excel.FaceDetection;
import com.crossover.report.excel.GenerateExcel;
import com.crossover.report.exception.ValidationException;
import com.crossover.report.service.ActivityGroups;
import com.crossover.report.service.DetailService;
import com.crossover.report.service.LoginService;
import com.crossover.report.service.TeamAssignementService;
import com.crossover.report.service.WorkDiariesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.crossover.report.exception.ValidationReason.DATE_RANGE_SHOULD_NOT_BE_MORE_THAN_30;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

@Component
public class AnalyzeManager {

    private static final Logger logger = LogManager.getLogger(AnalyzeManager.class);

    @Autowired
    LoginService loginService;

    @Autowired
    DetailService detailService;

    @Autowired
    TeamAssignementService teamAssignementService;

    @Autowired
    WorkDiariesService workDiariesService;

    @Autowired
    ActivityGroups activityGroupsService;

    @Value("${start.date}")
    String startDate;

    @Value("${end.date}")
    String endDate;

    @Value("${enable.face.detection}")
    boolean faceDetectionEnabled;

    @Autowired
    GenerateExcel generateExcel;

    private LocalDate startDateTemp;

    private int distractionTimeInMinutes = 0;
    private static String folderName = LocalDateTime.now().toString().replace(':', '-');

    int dailyIntnsityChartRow =-1;

    @SuppressWarnings("unchecked")
    public void analyze() {

        if (StringUtils.isEmpty(startDate)) {
            startDate = LocalDate.now().with(DayOfWeek.MONDAY).toString();
        }
        if (StringUtils.isEmpty(endDate)) {
            endDate = LocalDate.now().with(DayOfWeek.SUNDAY).toString();
        }

        long daysBetween = DAYS.between(LocalDate.parse(startDate), LocalDate.parse(endDate));
        if (daysBetween > 30) {
            throw new ValidationException(DATE_RANGE_SHOULD_NOT_BE_MORE_THAN_30);
        }

        Map map = new HashMap<>();
        LoginService.setToken(loginService.postEntity(Map.class, map).get("token").toString());
        Map<String, Object> userDetails = detailService.getEntity(Map.class);
        Map<String, Object> assignmentMap = (Map) userDetails.get("assignment");
        Map<String, Object> managerAvatarMap = (Map) userDetails.get("managerAvatar");
        Map<String, Object> teamAssignmentMap = teamAssignementService.getEntity(Map.class, "from=" + startDate, "to=" + endDate,
                "fullTeam=false&limit=1000&page=0&status=ACTIVE," +
                        "MANAGER_SETUP", "teamId=" + ((Map) assignmentMap.get("team")).get("id"), "managerId=" + managerAvatarMap.get("id"));
        List<Map<String, Object>> userLists = (List) teamAssignmentMap.get("content");

        List<Map<String, Object>> noFaceDataSet = new ArrayList<>();

        Map<String, Integer> noOfBadScoreDataSet = new HashMap<>();

        final DefaultCategoryDataset dataSetForDistraction = new DefaultCategoryDataset();

        final DefaultCategoryDataset noOfBadIntensityDataScore = new DefaultCategoryDataset();
        final DefaultPieDataset dataSetTopDistractionApplication = new DefaultPieDataset();
        final Map<String, Integer> topDistractionApplication = new HashMap<>();
        List<String> allUsers = new ArrayList<>();
        Set<String> allDates = new LinkedHashSet<>();
        for (Map<String, Object> singleUser : userLists) {

            final List<Map<String, Object>> dataSetForEvery10Minutes = new ArrayList<>();
            Map candidateMap = (Map) singleUser.get("candidate");
            String userName = candidateMap.get("userId") + "-" + candidateMap.get("firstName")
                    + " " + candidateMap.get("lastName");
            allUsers.add(userName);

            startDateTemp = LocalDate.parse(startDate);
            while (startDateTemp.isBefore(LocalDate.parse(endDate).plusDays(1))) {
                final DefaultCategoryDataset dataSetForDailyIntensityChart = new DefaultCategoryDataset();
                dailyIntnsityChartRow++;
                allDates.add(candidateMap.get("firstName")+"_"+ candidateMap.get("lastName")+"_"+candidateMap.get("userId")+"_"+startDate.replace('-','_'));
                if (!noOfBadScoreDataSet.containsKey(userName + ":" + startDateTemp.toString())) {
                    noOfBadScoreDataSet.put(userName + ":" + startDateTemp.toString(), 0);
                }
                logger.info(userName + "-" + startDateTemp.toString());
                List maps;
                try {
                    maps = workDiariesService.getEntity(List.class, "assignmentId=" + singleUser.get("id"), "date=" + startDateTemp);
                } catch (HttpClientErrorException forBiddenExcepctionn) {
                    startDateTemp = startDateTemp.plusDays(1);
                    continue;
                }
                List groupingList;
                try {
                    groupingList = activityGroupsService.getEntity(List.class, "assignmentId=" + singleUser.get("id"), "date=" +
                            startDateTemp, "teamId=" + ((Map) assignmentMap.get("team")).get("id"));
                } catch (HttpClientErrorException e) {
                    startDateTemp = startDateTemp.plusDays(1);
                    continue;
                }

                groupingList.forEach(singleGroup -> {
                    List list = ((List) ((Map) ((Map) singleGroup).get("grouping")).get("advancedGroups"));
                    for (Object object : list) {
                        if (((Map) object).get("sectionName").equals("Distraction")) {
                            List distractionList = (List) ((Map) object).get("groupItems");
                            for (Object singleDistraction : distractionList) {
                                int distractionTimeLocal = ((Number) ((Map) singleDistraction).get("spentTime")).intValue();
                                distractionTimeInMinutes += distractionTimeLocal;
                                String applicationName = ((Map) singleDistraction).get("applicationName").toString();
                                if (!topDistractionApplication.containsKey(applicationName)) {
                                    topDistractionApplication.put(applicationName, 0);
                                }
                                topDistractionApplication.put(applicationName, topDistractionApplication.get(applicationName)
                                        + distractionTimeLocal);
                            }
                        }
                    }

                });

                maps.forEach((Object singleEntry) -> {
                    Map<String, Object> singleRow = new HashMap<>();
                    singleRow.put("date", ((Map) singleEntry).get("date"));
                    singleRow.put("activityLevel", ((Map) singleEntry).get("activityLevel"));
                    singleRow.put("mouseEvents", ((Map) singleEntry).get("mouseEvents"));
                    singleRow.put("keyboardEvents", ((Map) singleEntry).get("keyboardEvents"));
                    singleRow.put("intensityScore", ((Map) singleEntry).get("intensityScore"));
                    singleRow.put("windowTitle", ((Map) singleEntry).get("windowTitle"));
                    String url = (((Map) singleEntry).get("webcam")) != null ? ((Map) ((Map) singleEntry).get("webcam")).get("url")
                            .toString() : null;
                    if (((Number) ((Map) singleEntry).get("intensityScore")).intValue() <= 30) {
                        noOfBadScoreDataSet.put(userName + ":" + startDateTemp.toString(), noOfBadScoreDataSet.get
                                (userName + ":" + startDateTemp.toString()) + 1);
                    }
                    if (faceDetectionEnabled) {
                        if (!StringUtils.isEmpty(url) && !FaceDetection.hasFace(folderName, ((Map) singleEntry).get("date").toString()
                                        .replace(':', '-') + "-" + userName,
                                url)) {
                            Map<String, Object> singleRowNoFace = new HashMap<>();
                            singleRowNoFace.put("date", ((Map) singleEntry).get("date"));
                            singleRowNoFace.put("user", userName);
                            singleRowNoFace.put("url", url);
                            noFaceDataSet.add(singleRowNoFace);
                        }
                    }
                    dataSetForEvery10Minutes.add(singleRow);
                    dataSetForDailyIntensityChart.addValue(((Number)((Map) singleEntry).get("intensityScore")).intValue(),userName, ((Map)
                            singleEntry).get("time").toString());
                });



                dataSetForDistraction.addValue(distractionTimeInMinutes, userName,
                        startDateTemp.toString());
                dailyIntesityChart(dataSetForDailyIntensityChart, dailyIntnsityChartRow,candidateMap.get("firstName")+"_"+ candidateMap
                      .get("lastName")+"_"+candidateMap.get("userId")+"_"+startDateTemp.toString().replace('-','_'));
                if (!noOfBadScoreDataSet.containsKey(userName + ":" + startDateTemp.toString()));
                startDateTemp = startDateTemp.plusDays(1);
            }
            generateExcel.print(dataSetForEvery10Minutes, userName + "-Activity");

        }

        //generateExcel.print("ValidationData", allUsers, 0);
        //generateExcel.print("ValidationData", allDates.stream().collect(Collectors.toList()), 0);

        Map<String, Integer> sorted = topDistractionApplication
                .entrySet()
                .stream()
                .sorted(comparingByValue()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        int i = 0;
        int others = 0;
        for (Map.Entry<String, Integer> singleEntry : sorted.entrySet()) {
            if (i < 10) {
                dataSetTopDistractionApplication.setValue(singleEntry.getKey(), singleEntry.getValue());
            } else {
                others += singleEntry.getValue();
            }
            i++;
        }
        dataSetTopDistractionApplication.setValue("All Other Applications", others);

        drawBarChart(dataSetForDistraction, daysBetween);
        drawLineChart(dataSetForDistraction, daysBetween);
        drawPieChartForTop10Distraciton(dataSetTopDistractionApplication);
        noOfBadScoreDataSet.forEach((key, value) -> noOfBadIntensityDataScore.addValue(value, key.split(":")[0], key.split(":")[1]));
        if (faceDetectionEnabled) {
            generateExcel.print(noFaceDataSet, "No Face Data");
        }
        drawBarChartForMostBadIntensity(noOfBadIntensityDataScore, daysBetween);
        drawLineChartForMostBadIntensity(noOfBadIntensityDataScore, daysBetween);
        generateExcel.writeData();
    }

    private void drawPieChartForTop10Distraciton(PieDataset dataset) {

        JFreeChart pieChart3D = ChartFactory.createPieChart3D(
                "Top Distractions",  // chart title
                 dataset,         // data
                true,            // include legend
                true,
                false);

        int width = 640;    /* Width of the image */
        int height = 480;   /* Height of the image */
        ByteArrayOutputStream barChartOutput = new ByteArrayOutputStream();
        try {
            ChartUtils.writeChartAsJPEG(barChartOutput, pieChart3D, width, height);
        } catch (IOException e) {
            logger.error("Error Writing Chart", e);
        }
        generateExcel.createDrawing(barChartOutput, "Top Distraction Item", height, width);
    }

    private void dailyIntesityChart(DefaultCategoryDataset dataset, int row, String name) {

        JFreeChart barChart = ChartFactory.createLineChart(
                "Intensity Time Chart",
                "Distraction", "Time in Minutes",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        int width = 5000 ;   /* Width of the image */
        int height = 480;   /* Height of the image */
        ByteArrayOutputStream barChartOutput = new ByteArrayOutputStream();
        try {
            ChartUtils.writeChartAsJPEG(barChartOutput, barChart, width, height);
        } catch (IOException e) {
            logger.error("Error Writing Chart", e);
        }
        generateExcel.createDrawing(barChartOutput, "DailyIntensityReport", height, width, row, name);
    }


    private void drawLineChart(DefaultCategoryDataset dataset, long daysBetween) {

        JFreeChart barChart = ChartFactory.createLineChart(
                "Distraction Time Chart",
                "Distraction", "Time in Minutes",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        int width = (int) (200 + (daysBetween * 200));    /* Width of the image */
        int height = 480;   /* Height of the image */
        ByteArrayOutputStream barChartOutput = new ByteArrayOutputStream();
        try {
            ChartUtils.writeChartAsJPEG(barChartOutput, barChart, width, height);
        } catch (IOException e) {
            logger.error("Error Writing Chart", e);
        }
        generateExcel.createDrawing(barChartOutput, "Distraction Line Chart", height, width);
    }

    private void drawBarChart(DefaultCategoryDataset dataset, long daysBetween) {

        JFreeChart barChart = ChartFactory.createBarChart(
                "Distraction Time Chart",
                "Distraction", "Time in Minutes",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        int width = (int) (200 + (daysBetween * 200));    /* Width of the image */
        int height = 480;   /* Height of the image */
        ByteArrayOutputStream barChartOutput = new ByteArrayOutputStream();
        try {
            ChartUtils.writeChartAsJPEG(barChartOutput, barChart, width, height);
        } catch (IOException e) {
            logger.error("Error Writing Chart", e);
        }
        generateExcel.createDrawing(barChartOutput, "Distraction Bar Chart", height, width);
    }

    private void drawBarChartForMostBadIntensity(DefaultCategoryDataset dataset, long daysBetween) {

        JFreeChart barChart = ChartFactory.createBarChart(
                "Most Bad Intensity",
                "Bad Intensity", "No Of Count",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        long width = 200 + (daysBetween * 200);    /* Width of the image */
        int height = 480;   /* Height of the image */
        ByteArrayOutputStream barChartOutput = new ByteArrayOutputStream();
        try {
            ChartUtils.writeChartAsJPEG(barChartOutput, barChart, (int) width, height);
        } catch (IOException e) {
            logger.error("Error Writing Chart", e);
        }
        generateExcel.createDrawing(barChartOutput, "Most-Bad-Intensity-Bar", height,(int) width);
    }

    private void drawLineChartForMostBadIntensity(DefaultCategoryDataset dataset, long daysBetween) {

        JFreeChart barChart = ChartFactory.createLineChart(
                "Most Bad Intensity",
                "Bad Intensity", "No Of Count",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        long width = 200 + (daysBetween * 200);    /* Width of the image */
        int height = 480;   /* Height of the image */
        ByteArrayOutputStream barChartOutput = new ByteArrayOutputStream();
        try {
            ChartUtils.writeChartAsJPEG(barChartOutput, barChart, (int) width, height);
        } catch (IOException e) {
            logger.error("Error Writing Chart", e);
        }
        generateExcel.createDrawing(barChartOutput, "Most-Bad-Intensity-Line", height,(int) width);
    }
}