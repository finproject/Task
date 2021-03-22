package com.example.criteria.service;

import com.example.criteria.convert.dto.SearchAuto;
import com.example.criteria.convert.dto.SearchParameterAuto;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;

@Service
public class FindAutoByParameters {

    @PersistenceContext
    private EntityManager entityManager;

    public Query findAuto(SearchAuto auto, SearchParameterAuto parametersAuto) {

        StringBuffer query1 = new StringBuffer();
        
        query1.append("SELECT a.id, a.model, a.place, a.year, p.color, p.hand, ");
        query1.append("p.kilometers, p.type_of_engine, p.type_of_ownership ");
        query1.append("FROM auto a ");
        query1.append("INNER JOIN parameters p ");
        query1.append("ON a.id=p.id ");
        query1.append("WHERE ");

        ArrayList<String> criteria = new ArrayList<>();
        if (auto.getModel() != null)
            criteria.add("a.model ='" + auto.getModel() + "'");
        if (auto.getPlace() != null)
            criteria.add("a.place ='" + auto.getPlace() + "'");
        if (auto.getYear() > 1900)
            criteria.add("a.year ='" + auto.getYear() + "'");
        if (parametersAuto.getColor() != null)
            criteria.add("p.color ='" + parametersAuto.getColor() + "'");
        if (parametersAuto.getHand() > 0)
            criteria.add("p.hand ='" + parametersAuto.getHand() + "'");
        if (parametersAuto.getKilometers() > 0)
            criteria.add("p.kilometers ='" + parametersAuto.getKilometers() + "'");
        if (parametersAuto.getType_of_engine() != null)
            criteria.add("p.type_of_engine ='" + parametersAuto.getType_of_engine() + "'");
        if (parametersAuto.getType_of_ownership() != null)
            criteria.add("p.type_of_ownership ='" + parametersAuto.getType_of_ownership() + "'");

        for (int i = 0; i < criteria.size(); i++) {
            if (i > 0) {
                query1.append(" AND ");
            }
            query1.append(criteria.get(i));
        }
        query1.append(";");
        Query query = entityManager.createQuery(query1.toString());
        System.out.println(query.getFirstResult());
        return query;
    }
}
