/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.persistence.xstream.impl.testdata.domain;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.score.buildin.simple.SimpleScoreDefinition;
import org.optaplanner.core.impl.testdata.domain.TestdataObject;
import org.optaplanner.persistence.xstream.impl.score.XStreamScoreConverter;

@PlanningSolution
@XStreamAlias("xStreamTestdataSolution")
public class XStreamTestdataSolution extends TestdataObject {

    public static SolutionDescriptor<XStreamTestdataSolution> buildSolutionDescriptor() {
        return SolutionDescriptor.buildSolutionDescriptor(XStreamTestdataSolution.class, XStreamTestdataEntity.class);
    }

    private List<XStreamTestdataValue> valueList;
    private List<XStreamTestdataEntity> entityList;

    @XStreamConverter(value = XStreamScoreConverter.class, types = {SimpleScoreDefinition.class})
    private SimpleScore score;

    public XStreamTestdataSolution() {
    }

    public XStreamTestdataSolution(String code) {
        super(code);
    }

    @ValueRangeProvider(id = "valueRange")
    @ProblemFactCollectionProperty
    public List<XStreamTestdataValue> getValueList() {
        return valueList;
    }

    public void setValueList(List<XStreamTestdataValue> valueList) {
        this.valueList = valueList;
    }

    @PlanningEntityCollectionProperty
    public List<XStreamTestdataEntity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<XStreamTestdataEntity> entityList) {
        this.entityList = entityList;
    }

    @PlanningScore
    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
