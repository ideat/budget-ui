package com.mindware.backend.rest.reports;

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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
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
    private String title;
    private String previousPage;
    private FlexBoxLayout contentReport;


    private QueryParameters qp;
    private Map<String, List<String>> paramPrev;
    private boolean error;

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
            file = expenseAcquisitionsRestTemplate.reportExpenseAcquisition(period);
            paramPrev.put("period",param.get("period"));

        }else  if(param.get("origin").get(0).equals("acquisition-detail")){
            String period = param.get("period").get(0);
            previousPage = param.get("path").get(0);
            file = expenseAcquisitionsRestTemplate.reportAcquisitionDetail(period);
            paramPrev.put("period",param.get("period"));

        }else  if(param.get("origin").get(0).equals("basicrecurrent-detail")){
            String period = param.get("period").get(0);
            previousPage = param.get("path").get(0);
            file = expenseAcquisitionsRestTemplate.reportBasicRecurrentService(period);
            paramPrev.put("period",param.get("period"));
        }else  if(param.get("origin").get(0).equals("investment-detail")){
            String year = param.get("year").get(0);
            String codefatherBusinessUnit = param.get("codefatherbusinessunit").get(0);
            String cutOffDate = param.get("cutoffdate").get(0);
            String nameBusinessUnit = param.get("namebusinessunit").get(0);

            previousPage = param.get("path").get(0);
            file = investmentBudgetRestTemplate.reportInvestmentBudgetDetail(year,codefatherBusinessUnit,cutOffDate,nameBusinessUnit);
//            paramPrev.put("year",param.get("year"));
        }else  if(param.get("origin").get(0).equals("investment-business-unit")){
            String cutOffDate = param.get("cutoffdate").get(0);

            previousPage = param.get("path").get(0);
            file = investmentBudgetRestTemplate.reportInvestmentBudgetGroupedBusinessUnit(cutOffDate);

        }else  if(param.get("origin").get(0).equals("investment-resume")){
            String cutOffDate = param.get("cutoffdate").get(0);

            previousPage = param.get("path").get(0);
            file = investmentBudgetRestTemplate.reportInvestmentBudgetExecutive(cutOffDate);

        }else  if(param.get("origin").get(0).equals("expenses-service-resume-businessunit")){
            String codefatherBusinessUnit = param.get("codefatherbusinessunit").get(0);
            String cutOffDate = param.get("cutoffdate").get(0);
            String nameBusinessUnit = param.get("namebusinessunit").get(0);

            previousPage = param.get("path").get(0);
            file = expenseServicesRestTemplate.reportExpensesServiceResume(codefatherBusinessUnit,cutOffDate,nameBusinessUnit);

        }else  if(param.get("origin").get(0).equals("expense-service-father-business-unit")){
            String codefatherBusinessUnit = param.get("codefatherbusinessunit").get(0);
            String cutOffDate = param.get("cutoffdate").get(0);
            String nameBusinessUnit = param.get("namebusinessunit").get(0);

            previousPage = param.get("path").get(0);
            file = expenseServicesRestTemplate.reportExpensesServiceBusinessUnit(codefatherBusinessUnit,cutOffDate,nameBusinessUnit);

        }else  if(param.get("origin").get(0).equals("expenses-service-detail")){
            String codefatherBusinessUnit = param.get("codefatherbusinessunit").get(0);
            String cutOffDate = param.get("cutoffdate").get(0);
            String nameBusinessUnit = param.get("namebusinessunit").get(0);

            previousPage = param.get("path").get(0);
            file = expenseServicesRestTemplate.reportExpensesServiceDetail(codefatherBusinessUnit,cutOffDate,nameBusinessUnit,"detail");

        }else  if(param.get("origin").get(0).equals("expenses-service-consolidated")){
            String cutOffDate = param.get("cutoffdate").get(0);

            previousPage = param.get("path").get(0);
            file = expenseServicesRestTemplate.reportExpenseConsolidated(cutOffDate);

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
        Div layout = new Div();
        layout.setHeightFull();
        ByteArrayInputStream bis = new ByteArrayInputStream(file);
        StreamResource s = new StreamResource("reporte.pdf", () -> bis);
        layout.add(new EmbeddedPdfDocument(s));

        DetailsDrawer detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.BOTTOM);
        detailsDrawer.setHeight("90%");
        detailsDrawer.setWidthFull();
        detailsDrawer.setContent(layout);
        return detailsDrawer;

    }
}
