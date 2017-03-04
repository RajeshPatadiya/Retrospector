/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package retrospector.fxml;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Callback;
import retrospector.model.DataManager;
import retrospector.model.Media;
import retrospector.model.Review;
import retrospector.util.NaturalOrderComparator;

/**
 * FXML Controller class
 *
 * @author nonfrt
 */
public class SearchTabController implements Initializable {

    @FXML
    private TextField searchBox;
    @FXML
    private TableView<Media> searchTable;
    @FXML
    private MenuButton searchNewMedia;
    @FXML
    private Button searchEditMedia;
    @FXML
    private Button searchDeleteMedia;
    @FXML
    private TableColumn<Media, Integer> searchNumberColumn;
    @FXML
    private TableColumn<Media, String> searchTitleColumn;
    @FXML
    private TableColumn<Media, String> searchCreatorColumn;
    @FXML
    private TableColumn<Media, String> searchSeasonColumn;
    @FXML
    private TableColumn<Media, String> searchEpisodeColumn;
    @FXML
    private TableColumn<Media, Integer> searchReviewsColumn;
    @FXML
    private TableColumn<Media, String> searchCategoryColumn;
    @FXML
    private TableColumn<Media, ?> searchRatingColumns;
    @FXML
    private TableColumn<Media, BigDecimal> searchMeanRColumn;
    @FXML
    private TableColumn<Media, BigDecimal> searchCurrentRColumn;
    @FXML
    private MenuItem searchQuickEntry;
    @FXML
    private MenuItem searchStandardEntry;
    @FXML
    private Text searchMeanAverage;
    @FXML
    private Text searchCurrentAverage;
    
    private ObservableList<Media> searchTableData;
    private Media currentMedia;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initSearchTab();
    }   
    
    private void updateSearchTab(){
        searchTableData.clear();
        searchTableData.addAll(DataManager.getMedia());
        searchTable.refresh();
        if(searchTable.getItems().contains(getMedia()))
            searchTable.getSelectionModel().select(getMedia());
        else if(searchTable.getItems().size()>0)
            searchTable.getSelectionModel().select(0);
    }
    
    public Media getMedia(){
        return currentMedia;
    }
    
    public void update(Media m){
        setMedia(m);
    }
    
    private void setMedia(Media m){
        currentMedia = m;
    }
    
    public void refresh(){
        updateSearchTab();
    }
    
    private void initSearchTab(){
        searchTableData = DataManager.getMedia();
        FilteredList<Media> mediaFiltered = new FilteredList(searchTableData,x->true);
        SortedList<Media> mediaSortable = new SortedList<>(mediaFiltered);
        searchTable.setItems(mediaSortable);
        mediaSortable.comparatorProperty().bind(searchTable.comparatorProperty());
        
        // Link to data properties
        searchTitleColumn.setCellValueFactory(new PropertyValueFactory<Media,String>("Title"));
        searchCreatorColumn.setCellValueFactory(new PropertyValueFactory<Media,String>("Creator"));
        searchSeasonColumn.setCellValueFactory(new PropertyValueFactory<Media,String>("SeasonId"));
        searchEpisodeColumn.setCellValueFactory(new PropertyValueFactory<Media,String>("EpisodeId"));
        searchCategoryColumn.setCellValueFactory(new PropertyValueFactory<Media,String>("Category"));
        
        // Values for special columns
        searchNumberColumn.setSortable(false);
        searchNumberColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(1+searchTable.getItems().indexOf(p.getValue())));
        searchReviewsColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Media,Integer>, ObservableValue<Integer>>() {
            @Override
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Media,Integer> p) {
                return new ReadOnlyObjectWrapper(p.getValue().getReviews().size());
            }
        });
        searchMeanRColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Media,BigDecimal>, ObservableValue<BigDecimal>>() {
            @Override
            public ObservableValue<BigDecimal> call(TableColumn.CellDataFeatures<Media,BigDecimal> p) {
                return new ReadOnlyObjectWrapper(p.getValue().getAverageRating());
            }
        });
//        searchOriginalRColumn.setCellValueFactory(new Callback<CellDataFeatures<Media,BigDecimal>, ObservableValue<BigDecimal>>() {
//            @Override
//            public ObservableValue<BigDecimal> call(CellDataFeatures<Media,BigDecimal> p) {
//                return new ReadOnlyObjectWrapper(p.getValue().getOriginalRating());
//            }
//        });
        searchCurrentRColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Media,BigDecimal>, ObservableValue<BigDecimal>>() {
            @Override
            public ObservableValue<BigDecimal> call(TableColumn.CellDataFeatures<Media,BigDecimal> p) {
                return new ReadOnlyObjectWrapper(p.getValue().getCurrentRating());
            }
        });
        
        // Comparors for string columns
        searchTitleColumn.setComparator(new NaturalOrderComparator());
        searchCreatorColumn.setComparator(new NaturalOrderComparator());
        searchSeasonColumn.setComparator(new NaturalOrderComparator());
        searchEpisodeColumn.setComparator(new NaturalOrderComparator());
        searchCategoryColumn.setComparator(new NaturalOrderComparator());
        
        searchTable.getSelectionModel().selectedItemProperty().addListener( (observe, old, neo) -> {
            setMedia(neo);
        });
        
        searchBox.textProperty().addListener((observa,old,neo)->{
            String query = neo.toLowerCase();
            if(query==null || query.equals(""))
                mediaFiltered.setPredicate(x->true);
            else{
                String[] queries = query.split(":");
                mediaFiltered.setPredicate(x->{
                    boolean pass = true;
                    for (String q : queries) {
                        String[] optns = q.split("\\|\\|");
                        boolean minorPass = false;
                        for (String optn : optns) {
                            if(
                                    x.getTitle().toLowerCase().contains(optn) ||
                                    x.getCreator().toLowerCase().contains(optn) ||
                                    x.getSeasonId().toLowerCase().contains(optn) ||
                                    x.getEpisodeId().toLowerCase().contains(optn) ||
                                    x.getCategory().toLowerCase().contains(optn) 
                                    )
                                minorPass = true;
                        }
                        if(!minorPass)
                            pass = false;
                    }
                    return pass;
                });
            }
            int totalNumberReviews = 0;
            int totalNumberMedia = 0;
            int totalReviewRating = 0;
            int totalCurrentRating = 0;

            totalNumberMedia = searchTable.getItems().size();
            for(Media media : searchTable.getItems()){
                totalNumberReviews += media.getReviews().size();
                for (Review review : media.getReviews()) {
                    totalReviewRating += review.getRating().intValue();
                }
                totalCurrentRating += media.getCurrentRating().intValue();
            }

            searchMeanAverage.setText(String.format("%.2f", totalReviewRating*1.0/totalNumberReviews));
            searchCurrentAverage.setText(String.format("%.2f", totalCurrentRating*1.0/totalNumberMedia));
        });
        
        searchNewMedia.setOnAction(e->{
//            Media neo = new Media();
//            neo.setId(DataManager.createDB(neo));
//            setMedia(neo);
            throw new UnsupportedOperationException("No Media Tab interaction");
//            anchorCenter.getSelectionModel().select(mediaTab);
        });
        searchQuickEntry.setOnAction(e->{
                throw new UnsupportedOperationException("No Quick Entry interaction");
//                  try{
//                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("QuickEntry.fxml"));
//                    Parent root1 = (Parent) fxmlLoader.load();
//                    QuickEntryController qec = fxmlLoader.getController();
//                    qec.setCore(this);
//                    Stage stage = new Stage();
//                    stage.setTitle("Quick Entry");
//                    stage.setScene(new Scene(root1));  
//                    stage.show();
//                  } catch(Exception ex) {}
        });
        searchStandardEntry.setOnAction(e->{
            throw new UnsupportedOperationException("No Media Tab interaction");
//            Media neo = new Media();
//            neo.setId(DataManager.createDB(neo));
//            setMedia(neo);
//            anchorCenter.getSelectionModel().select(mediaTab);
        });
        searchEditMedia.setOnAction(e->{
            throw new UnsupportedOperationException("No Media Tab interaction");
//            anchorCenter.getSelectionModel().select(mediaTab);
        });
        searchDeleteMedia.setOnAction(e->{
            if(new Alert(Alert.AlertType.WARNING,"Are you sure you want to delete this?",ButtonType.NO,ButtonType.YES).showAndWait().get().equals(ButtonType.YES)){
                DataManager.deleteDB(getMedia());
                updateSearchTab();
            }
        });
    }
}