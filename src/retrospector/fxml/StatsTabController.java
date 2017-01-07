/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package retrospector.fxml;

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.text.Text;
import retrospector.model.DataManager;
import retrospector.model.Media;
import retrospector.model.Review;
import retrospector.util.Stroolean;

/**
 * FXML Controller class
 *
 * @author nonfrt
 */
public class StatsTabController implements Initializable {

    private List<Stroolean> strooleans = new ArrayList<>();
    private Media currentMedia;
    
    @FXML
    private PieChart chartMediaPerCategory;
    @FXML
    private ListView<Stroolean> userList;
    @FXML
    private ChoiceBox<String> categorySelector;
    @FXML
    private CheckBox checkTitle;
    @FXML
    private CheckBox checkCreator;
    @FXML
    private CheckBox checkSeason;
    @FXML
    private CheckBox checkEpisode;
    @FXML
    private CheckBox checkCategory;
    @FXML
    private Text overallMedia;
    @FXML
    private Text overallReview;
    @FXML
    private Text overallUser;
    @FXML
    private Text overallTime;
    @FXML
    private Text overallPerMonth;
    @FXML
    private Text categoryMedia;
    @FXML
    private Text categoryUser;
    @FXML
    private Text categoryTime;
    @FXML
    private Text categorySingle;
    @FXML
    private Text categoryMiniseries;
    @FXML
    private Text categorySeries;
    @FXML
    private Text categoryPerMonth;
    @FXML
    private TableView<Media> mediaTable;
    @FXML
    private TableColumn<Media, Integer> mediaColumnRowNumber;
    @FXML
    private TableColumn<Media, String> mediaColumnTitle;
    @FXML
    private TableColumn<Media, String> mediaColumnCreator;
    @FXML
    private TableColumn<Media, String> mediaColumnSeason;
    @FXML
    private TableColumn<Media, String> mediaColumnEpisode;
    @FXML
    private TableColumn<Media, String> mediaColumnCategory;
    @FXML
    private Text mediaMedia;
    @FXML
    private Text mediaReview;
    @FXML
    private Text mediaUser;
    @FXML
    private Text mediaTime;
    @FXML
    private Text mediaCurrentRating;
    @FXML
    private Text mediaAllRating;
    @FXML
    private Text overallSingle;
    @FXML
    private Text overallMiniseries;
    @FXML
    private Text overallSeries;
    @FXML
    private Text overallCurrentRating;
    @FXML
    private Text overallAllRating;
    @FXML
    private Text categoryCurrentRating;
    @FXML
    private Text categoryAllRating;
    @FXML
    private Text mediaSeries;
    @FXML
    private Text mediaMiniseries;
    @FXML
    private Text mediaPerMonth;
    @FXML
    private Text categoryReview;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chartMediaPerCategory.setLegendVisible(true);
        categorySelector.setItems(FXCollections.observableArrayList(DataManager.getCategories()));
        categorySelector.setValue(DataManager.getCategories()[0]);
        categorySelector.valueProperty().addListener((observe,old,neo)->updateCategory());
        userList.setVisible(false);
    }
    
       
    public void update(Media media){
        currentMedia = media;
        checkTitle.setText("Title: "+media.getTitle());
        checkCreator.setText("Creator: "+media.getCreator());
        checkSeason.setText("Season: "+media.getSeasonId());
        checkEpisode.setText("Episode: "+media.getEpisodeId());
        checkCategory.setText("Category: "+media.getCategory());
        update();
    }
    public void update(){
        
       updateOverall();
       updateCategory();
       updateMedia();
    }
    
    private void updateOverall(){
        
        // Data Mining - Vars
        List<Media> considerMedia = DataManager.getMedia();
        List<Review> considerReview = DataManager.getReviews();
        Map<String, Integer> categories = new HashMap<>();
        int media = considerMedia.size();
        int reviews = considerReview.size();
        int users = DataManager.getUsers().size();
        double aveAll = 0;
        double aveCurrent = 0;
        LocalDate earliest = LocalDate.now();
        int singles = 0;
        int minis = 0;
        int series = 0;
        long days = 0;
        double perMonth = 0;
        
        // Data Mining - Calcs
        for (Media m : considerMedia) {
            switch(m.getType()){
                case SINGLE:singles++;break;
                case MINISERIES:minis++;break;
                case SERIES:series++;break;
            }
            aveCurrent += m.getCurrentRating().intValue();
            categories.put(m.getCategory(), categories.getOrDefault(m.getCategory(), 0)+1);
        }
        for (Review r : considerReview) {
            if(r.getDate().isBefore(earliest))
                earliest = r.getDate();
            aveAll += r.getRating().intValue();
        }
        aveAll = reviews==0? 0 : aveAll/reviews;
        aveCurrent = reviews==0? 0 : aveCurrent/media;
        days = ChronoUnit.DAYS.between(earliest, LocalDate.now())+1;
        perMonth = days==0? 0 : (media+0.0)/days*30;
        
        // User List
        userList.getItems().clear();
        strooleans.clear();
        for (String category : DataManager.getUsers()) {
            Stroolean c = new Stroolean(category,true);
            c.booleanProperty().addListener((observe,old,neo)->update());
            strooleans.add(c);
            userList.getItems().add(c);
        }
        userList.setCellFactory(CheckBoxListCell.forListView(Stroolean::booleanProperty));
        
        // Stats
        overallMedia.setText(media+" Media");
        overallReview.setText(reviews+" Review(s)");
        overallUser.setText(users+" User(s)");  
        overallTime.setText(days+" Days");
        overallSingle.setText(singles+" Single(s)");
        overallMiniseries.setText(minis+" Mini(s)");
        overallSeries.setText(series+" Serie(s)");
        overallPerMonth.setText(String.format("%.2f", perMonth)+"/ Month");
        overallCurrentRating.setText(String.format("%.2f", aveCurrent)+" Current");
        overallAllRating.setText(String.format("%.2f", aveAll)+" All");
        
        // Chart
        chartMediaPerCategory.setData(
                FXCollections.observableArrayList(
                    categories.keySet()
                        .stream()
                        .map(c -> {
                                int count = categories.getOrDefault(c, 0);
                                return new PieChart.Data(c + " - " + count, count);
                            }
                        )
                        .collect(Collectors.toList())
                )
        );
    }
    
    private void updateCategory(){
        
        // Category Chooser
        String category = categorySelector.getValue();
            
        // Data Mining - Vars
        List<Media> considerMedia = DataManager.getMedia();
        List<String> userSet = new ArrayList<>();
        int media = 0;
        int reviews = 0;
        long users = 0;
        double aveAll = 0;
        double aveCurrent = 0;
        LocalDate earliest = LocalDate.now();
        int singles = 0;
        int minis = 0;
        int series = 0;
        long days = 0;
        double perMonth = 0;
        
        // Data Mining - Calcs
        for (Media m : considerMedia) {
            System.out.println(category);
            if(category.equals(m.getCategory())){
                switch(m.getType()){
                    case SINGLE:singles++;break;
                    case MINISERIES:minis++;break;
                    case SERIES:series++;break;
                }
                aveCurrent += m.getCurrentRating().intValue();
                media++;
                for (Review r : m.getReviews()) {
                    if(r.getDate().isBefore(earliest))
                        earliest = r.getDate();
                    aveAll += r.getRating().intValue();
                    userSet.add(r.getUser());
                    reviews++;
                }
            }
        }
        users = userSet.stream().distinct().count();
        aveAll = reviews==0? 0 : aveAll/reviews;
        aveCurrent = reviews==0? 0 : aveCurrent/media;
        days = ChronoUnit.DAYS.between(earliest, LocalDate.now())+1;
        perMonth = days==0? 0 : (media+0.0)/days*30;
        
        // Stats
        categoryMedia.setText(media+" Media");
        categoryReview.setText(reviews+" Review(s)");
        categoryUser.setText(users+" User(s)");
        categoryTime.setText(days+" Days");
        categorySingle.setText(singles+" Single(s)");
        categoryMiniseries.setText(minis+" Mini(s)");
        categorySeries.setText(series+" Serie(s)");
        categoryPerMonth.setText(String.format("%.2f", perMonth)+"/ Month");
        categoryCurrentRating.setText(String.format("%.2f", aveCurrent)+" Current");
        categoryAllRating.setText(String.format("%.2f", aveAll)+" All");
    }
    
    private void updateMedia(){
        
    }
}