/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.quarkus.smallrye.context.deployment;

import org.jboss.logging.Logger;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExecutorBuildItem;
import io.quarkus.smallrye.context.runtime.SmallRyeContextPropagationProvider;
import io.quarkus.smallrye.context.runtime.SmallRyeContextPropagationTemplate;

/**
 * The deployment processor for MP-CP applications
 */
class SmallRyeContextPropagationProcessor {
    private static final Logger log = Logger.getLogger(SmallRyeContextPropagationProcessor.class.getName());

    @BuildStep
    AdditionalBeanBuildItem registerBean() {
        return AdditionalBeanBuildItem.builder().addBeanClass(SmallRyeContextPropagationProvider.class).build();
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void build(SmallRyeContextPropagationTemplate template,
            BeanContainerBuildItem beanContainer,
            ExecutorBuildItem executorBuildItem) {
        template.configure(beanContainer.getValue(), executorBuildItem.getExecutorProxy());
    }
}
