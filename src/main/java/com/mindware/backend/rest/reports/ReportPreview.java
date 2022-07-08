package com.mindware.backend.rest.reports;

import com.mindware.backend.util.DownloadLink;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.components.detailsdrawer.DetailsDrawer;
import com.mindware.ui.components.navigation.bar.AppBar;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.EmbeddedPdfDocument;
import com.mindware.ui.util.css.BoxSizing;
import com.mindware.ui.views.SplitViewFrame;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import dev.mett.vaadin.tooltip.Tooltips;
import dev.mett.vaadin.tooltip.util.TooltipsUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "report-preview", layout = MainLayout.class)
@PageTitle("Previsualizacion Reporte")
public class ReportPreview extends SplitViewFrame implements HasUrlParameter<String> {

    @Autowired
    private ExpenseAcquisitionsRestTemplate expenseAcquisitionsRestTemplate;

    @Autowired
    private InvestmentBudgetRestTemplate investmentBudgetRestTemplate;

    @Autowired
    private ExpenseServicesRestTemplate expenseServicesRestTemplate;

    private byte[] file;
    private File  fileExcel;
    private String title;
    private String previousPage;
    private FlexBoxLayout contentReport;


    private QueryParameters qp;
    private Map<String, List<String>> paramPrev;
    private boolean error;
    private String pathfile;

    @Override
    public void setParameter(BeforeEvent beforeEvent,  @OptionalParameter String parameter) {
        error=false;

        Location location = beforeEvent.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        Map<String, List<String>> param = queryParameters.getParameters();
        paramPrev = new HashMap<>();

        if(param.get("origin").get(0).equals("expense-acquisition")){
            String period = param.get("period").get(0);
            previousPage = param.get("path").get(0);
            file = expenseAcquisitionsRestTemplate.reportExpenseAcquisition(period,"pdf");
            paramPrev.put("period",param.get("period"));
            pathfile = expenseAcquisitionsRestTemplate.reportExpenseAcquisitionXls(period,"xls");
            fileExcel = new File(pathfile.toString());

        }else  if(param.get("origin").get(0).equals("acquisition-detail")){
            String period = param.get("period").get(0);
            previousPage = param.get("path").get(0);
            file = expenseAcquisitionsRestTemplate.reportAcquisitionDetail(period,"pdf");
            paramPrev.put("period",param.get("period"));
            pathfile = expenseAcquisitionsRestTemplate.reportAcquisitionDetailXls(period,"xls");
            fileExcel = new File(pathfile.toString());

        }else  if(param.get("origin").get(0).equals("basicrecurrent-detail")){
            String period = param.get("period").get(0);
            previousPage = param.get("path").get(0);
            file = expenseAcquisitionsRestTemplate.reportBasicRecurrentService(period,"pdf");
            paramPrev.put("period",param.get("period"));
            pathfile = expenseAcquisitionsRestTemplate.reportBasicRecurrentServiceXls(period,"xls");
            fileExcel = new File(pathfile.toString());
        }else  if(param.get("origin").get(0).equals("investment-detail")){
            String year = param.get("year").get(0);
            String codefatherBusinessUnit = param.get("codefatherbusinessunit").get(0);
            String cutOffDate = param.get("cutoffdate").get(0);
            String nameBusinessUnit = param.get("namebusinessunit").get(0);

            previousPage = param.get("path").get(0);
            file = investmentBudgetRestTemplate.reportInvestmentBudgetDetail(year,codefatherBusinessUnit,cutOffDate,nameBusinessUnit,"pdf");
            pathfile = investmentBudgetRestTemplate.reportInvestmentBudgetDetailXls(year,codefatherBusinessUnit,cutOffDate,nameBusinessUnit,"xls");
            fileExcel = new File(pathfile.toString());
//            paramPrev.put("year",param.get("year"));
        }else  if(param.get("origin").get(0).equals("investment-business-unit")){
            String cutOffDate = param.get("cutoffdate").get(0);

            previousPage = param.get("path").get(0);
            file = investmentBudgetRestTemplate.reportInvestmentBudgetGroupedBusinessUnit(cutOffDate,"pdf");
            pathfile = investmentBudgetRestTemplate.reportInvestmentBudgetGroupedBusinessUnitXls(cutOffDate,"xls");
            fileExcel = new File(pathfile.toString());

        }else  if(param.get("origin").get(0).equals("investment-resume")){
            String cutOffDate = param.get("cutoffdate").get(0);

            previousPage = param.get("path").get(0);
            file = investmentBudgetRestTemplate.reportInvestmentBudgetExecutive(cutOffDate,"pdf");
            pathfile = investmentBudgetRestTemplate.reportInvestmentBudgetExecutiveXls(cutOffDate,"xls");
            fileExcel = new File(pathfile.toString());

        }else  if(param.get("origin").get(0).equals("expenses-service-resume-businessunit")){
            String codefatherBusinessUnit = param.get("codefatherbusinessunit").get(0);
            String cutOffDate = param.get("cutoffdate").get(0);
            String nameBusinessUnit = param.get("namebusinessunit").get(0);

            previousPage = param.get("path").get(0);
            file = expenseServicesRestTemplate.reportExpensesServiceResume(codefatherBusinessUnit,cutOffDate,nameBusinessUnit,"pdf");
            pathfile = expenseServicesRestTemplate.reportExpensesServiceResumeXls(codefatherBusinessUnit,cutOffDate,nameBusinessUnit,"xls");
            fileExcel = new File(pathfile.toString());

        }else  if(param.get("origin").get(0).equals("expense-service-father-business-unit")){
            String codefatherBusinessUnit = param.get("codefatherbusinessunit").get(0);
            String cutOffDate = param.get("cutoffdate").get(0);
            String nameBusinessUnit = param.get("namebusinessunit").get(0);

            previousPage = param.get("path").get(0);
            file = expenseServicesRestTemplate.reportExpensesServiceBusinessUnit(codefatherBusinessUnit,cutOffDate,nameBusinessUnit,"pdf");
            pathfile = expenseServicesRestTemplate.reportExpensesServiceBusinessUnitXls(codefatherBusinessUnit,cutOffDate,nameBusinessUnit,"xls");
            fileExcel = new File(pathfile.toString());

        }else  if(param.get("origin").get(0).equals("expenses-service-detail")){
            String codefatherBusinessUnit = param.get("codefatherbusinessunit").get(0);
            String cutOffDate = param.get("cutoffdate").get(0);
            String nameBusinessUnit = param.get("namebusinessunit").get(0);

            previousPage = param.get("path").get(0);
            file = expenseServicesRestTemplate.reportExpensesServiceDetail(codefatherBusinessUnit,cutOffDate,nameBusinessUnit,"detail","pdf");
            pathfile = expenseServicesRestTemplate.reportExpensesServiceDetailXls(codefatherBusinessUnit,cutOffDate,nameBusinessUnit,"detail","xls");
            fileExcel =  new File(pathfile.toString());

        }else  if(param.get("origin").get(0).equals("expenses-service-consolidated")){
            String cutOffDate = param.get("cutoffdate").get(0);

            previousPage = param.get("path").get(0);
            file = expenseServicesRestTemplate.reportExpenseConsolidated(cutOffDate,"pdf");
            pathfile = expenseServicesRestTemplate.reportExpenseConsolidatedXls(cutOffDate,"xls");
            fileExcel = new File(pathfile.toString());
        }

        if(!error) {
            qp = new QueryParameters(paramPrev);
            contentReport = (FlexBoxLayout) createContent(createReportView());
            setViewContent(contentReport);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent){
        super.onAttach(attachEvent);
        AppBar appBar =initAppBar();
        appBar.setTitle(title);
    }

    private AppBar initAppBar(){
        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.getContextIcon().addClickListener(
                e -> UI.getCurrent().navigate(previousPage,qp)
        );

        return appBar;
    }

    private Component createContent(DetailsDrawer component){
        FlexBoxLayout content = new FlexBoxLayout(component);
        content.setFlexDirection(FlexLayout.FlexDirection.ROW);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;
    }

    private DetailsDrawer createReportView(){
        VerticalLayout layout = new VerticalLayout();
        layout.setHeightFull();
        ByteArrayInputStream bis = new ByteArrayInputStream(file);
        StreamResource s = new StreamResource("reporte.pdf", () -> bis);

        Anchor export = createDownloadButton();
        layout.add(export, new EmbeddedPdfDocument(s));

        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END,export);

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setHeight("95%");
        detailsDrawer.setWidthFull();

        detailsDrawer.setContent(layout);
        return detailsDrawer;

    }

    private Anchor createDownloadButton() {

        Anchor downloadLink =  new Anchor(getStreamResource(fileExcel.getName(), fileExcel), " Descargar Excel");
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.removeAll();
        Button export = new Button();
        export.addThemeVariants(ButtonVariant.LUMO_SMALL,ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        export.setIcon(VaadinIcon.TABLE.create());
        Tooltips.getCurrent().setTooltip(export,"Exportar Excel");
        downloadLink.add(export);

        return downloadLink;
    }

    public StreamResource getStreamResource(String filename, File content) {
        return new StreamResource(filename, () -> {
            try {
                return new ByteArrayInputStream(FileUtils.readFileToByteArray(content));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
