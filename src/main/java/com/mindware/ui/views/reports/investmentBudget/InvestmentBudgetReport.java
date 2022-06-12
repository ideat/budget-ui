package com.mindware.ui.views.reports.investmentBudget;

import com.mindware.backend.util.UtilValues;
import com.mindware.ui.MainLayout;
import com.mindware.ui.components.FlexBoxLayout;
import com.mindware.ui.layout.size.Horizontal;
import com.mindware.ui.layout.size.Top;
import com.mindware.ui.util.css.BoxSizing;
import com.mindware.ui.views.ViewFrame;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "investment-budget-report", layout = MainLayout.class)
@ParentLayout(MainLayout.class)
@PageTitle("Reportes")
public class InvestmentBudgetReport  extends ViewFrame implements RouterLayout {

    private static final String INVESTMENT_BUDGET_RESUME ="RESUMEN EJECUTIVO";
    private static final String INVESTMENT_BUSINESS_UNIT ="CONSOLIDADO REGIONALES";
    private static final String INVESTMENT_BUDGET_DETAIL ="DETALLE INVERSIONES";

    @Autowired
    UtilValues utilValues;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        setViewContent(createContent());
    }

    private Accordion createOptionsReport() {
        Accordion optionDetails = new Accordion();

        AccordionPanel resumeInvestmentBudget = optionDetails
                .add(INVESTMENT_BUDGET_RESUME, layoutInvestmentBusinessUnit( "investment-resume","investment-budget-report"));
        AccordionPanel investmentBusinessUnit = optionDetails
                .add(INVESTMENT_BUSINESS_UNIT, layoutInvestmentBusinessUnit("investment-business-unit","investment-budget-report"));
        AccordionPanel investmentBudgetDetail = optionDetails
                .add(INVESTMENT_BUDGET_DETAIL, layoutInvestmentBusinessUnit("investment-detail","investment-budget-report"));

        return optionDetails;
    }

    private HorizontalLayout layoutInvestmentBusinessUnit(String originReport, String pathPage){

        ComboBox<Integer> years = new ComboBox<>();
        years.setWidth("200px");
        years.setItems(utilValues.getAllYears());
        years.setAllowCustomValue(false);
        years.setClearButtonVisible(true);
        years.setPlaceholder("Seleccione Gestión");

        Button print = new Button("Imprimir");
        print.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_CONTRAST);
        print.setIcon(VaadinIcon.PRINT.create());
        print.addClickListener(event ->{

            Map<String, List<String>> paramInvestmentBudget = new HashMap<>();
            List<String> origin = new ArrayList<>();
            origin.add(originReport);
            List<String> path = new ArrayList<>();
            path.add(pathPage);
            List<String> year = new ArrayList<>();
            year.add(years.getValue().toString());

            paramInvestmentBudget.put("origin",origin);
            paramInvestmentBudget.put("path",path);
            paramInvestmentBudget.put("year",year);
            QueryParameters qp = new QueryParameters(paramInvestmentBudget);
            UI.getCurrent().navigate("report-preview", qp);

        });

        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.add(years,print);

        return layout;
    }

    private Component createContent(){
        FlexBoxLayout content = new FlexBoxLayout(createOptionsReport());
//        content.addClassName("grid-view");
        content.setHeightFull();
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        return content;
    }
}
