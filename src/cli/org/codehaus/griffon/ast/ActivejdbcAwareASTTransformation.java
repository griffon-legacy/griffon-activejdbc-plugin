/*
 * Copyright 2012-2013 the original author or authors.
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

package org.codehaus.griffon.ast;

import griffon.plugins.activejdbc.DefaultActivejdbcProvider;
import griffon.plugins.activejdbc.ActivejdbcAware;
import griffon.plugins.activejdbc.ActivejdbcContributionHandler;
import griffon.plugins.activejdbc.ActivejdbcProvider;
import lombok.core.handlers.ActivejdbcAwareConstants;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.codehaus.griffon.ast.GriffonASTUtils.*;

/**
 * Handles generation of code for the {@code @ActivejdbcAware} annotation.
 * <p/>
 *
 * @author Andres Almiray
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ActivejdbcAwareASTTransformation extends AbstractASTTransformation implements ActivejdbcAwareConstants {
    private static final Logger LOG = LoggerFactory.getLogger(ActivejdbcAwareASTTransformation.class);
    private static final ClassNode ACTIVEJDBC_CONTRIBUTION_HANDLER_CNODE = makeClassSafe(ActivejdbcContributionHandler.class);
    private static final ClassNode ACTIVEJDBC_AWARE_CNODE = makeClassSafe(ActivejdbcAware.class);
    private static final ClassNode ACTIVEJDBC_PROVIDER_CNODE = makeClassSafe(ActivejdbcProvider.class);
    private static final ClassNode DEFAULT_ACTIVEJDBC_PROVIDER_CNODE = makeClassSafe(DefaultActivejdbcProvider.class);

    private static final String[] DELEGATING_METHODS = new String[] {
        METHOD_WITH_ACTIVEJDBC
    };

    static {
        Arrays.sort(DELEGATING_METHODS);
    }

    /**
     * Convenience method to see if an annotated node is {@code @ActivejdbcAware}.
     *
     * @param node the node to check
     * @return true if the node is an event publisher
     */
    public static boolean hasActivejdbcAwareAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (ACTIVEJDBC_AWARE_CNODE.equals(annotation.getClassNode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the bulk of the processing, mostly delegating to other methods.
     *
     * @param nodes  the ast nodes
     * @param source the source unit for the nodes
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        checkNodesForAnnotationAndType(nodes[0], nodes[1]);
        addActivejdbcContributionIfNeeded(source, (ClassNode) nodes[1]);
    }

    public static void addActivejdbcContributionIfNeeded(SourceUnit source, ClassNode classNode) {
        if (needsActivejdbcContribution(classNode, source)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Injecting " + ActivejdbcContributionHandler.class.getName() + " into " + classNode.getName());
            }
            apply(classNode);
        }
    }

    protected static boolean needsActivejdbcContribution(ClassNode declaringClass, SourceUnit sourceUnit) {
        boolean found1 = false, found2 = false, found3 = false, found4 = false;
        ClassNode consideredClass = declaringClass;
        while (consideredClass != null) {
            for (MethodNode method : consideredClass.getMethods()) {
                // just check length, MOP will match it up
                found1 = method.getName().equals(METHOD_WITH_ACTIVEJDBC) && method.getParameters().length == 1;
                found2 = method.getName().equals(METHOD_WITH_ACTIVEJDBC) && method.getParameters().length == 2;
                found3 = method.getName().equals(METHOD_SET_ACTIVEJDBC_PROVIDER) && method.getParameters().length == 1;
                found4 = method.getName().equals(METHOD_GET_ACTIVEJDBC_PROVIDER) && method.getParameters().length == 0;
                if (found1 && found2 && found3 && found4) {
                    return false;
                }
            }
            consideredClass = consideredClass.getSuperClass();
        }
        if (found1 || found2 || found3 || found4) {
            sourceUnit.getErrorCollector().addErrorAndContinue(
                new SimpleMessage("@ActivejdbcAware cannot be processed on "
                    + declaringClass.getName()
                    + " because some but not all of methods from " + ActivejdbcContributionHandler.class.getName() + " were declared in the current class or super classes.",
                    sourceUnit)
            );
            return false;
        }
        return true;
    }

    public static void apply(ClassNode declaringClass) {
        injectInterface(declaringClass, ACTIVEJDBC_CONTRIBUTION_HANDLER_CNODE);

        // add field:
        // protected ActivejdbcProvider this$activejdbcProvider = DefaultActivejdbcProvider.instance
        FieldNode providerField = declaringClass.addField(
            ACTIVEJDBC_PROVIDER_FIELD_NAME,
            ACC_PRIVATE | ACC_SYNTHETIC,
            ACTIVEJDBC_PROVIDER_CNODE,
            defaultActivejdbcProviderInstance());

        // add method:
        // ActivejdbcProvider getActivejdbcProvider() {
        //     return this$activejdbcProvider
        // }
        injectMethod(declaringClass, new MethodNode(
            METHOD_GET_ACTIVEJDBC_PROVIDER,
            ACC_PUBLIC,
            ACTIVEJDBC_PROVIDER_CNODE,
            Parameter.EMPTY_ARRAY,
            NO_EXCEPTIONS,
            returns(field(providerField))
        ));

        // add method:
        // void setActivejdbcProvider(ActivejdbcProvider provider) {
        //     this$activejdbcProvider = provider ?: DefaultActivejdbcProvider.instance
        // }
        injectMethod(declaringClass, new MethodNode(
            METHOD_SET_ACTIVEJDBC_PROVIDER,
            ACC_PUBLIC,
            ClassHelper.VOID_TYPE,
            params(
                param(ACTIVEJDBC_PROVIDER_CNODE, PROVIDER)),
            NO_EXCEPTIONS,
            block(
                ifs_no_return(
                    cmp(var(PROVIDER), ConstantExpression.NULL),
                    assigns(field(providerField), defaultActivejdbcProviderInstance()),
                    assigns(field(providerField), var(PROVIDER))
                )
            )
        ));

        for (MethodNode method : ACTIVEJDBC_CONTRIBUTION_HANDLER_CNODE.getMethods()) {
            if (Arrays.binarySearch(DELEGATING_METHODS, method.getName()) < 0) continue;
            List<Expression> variables = new ArrayList<Expression>();
            Parameter[] parameters = new Parameter[method.getParameters().length];
            for (int i = 0; i < method.getParameters().length; i++) {
                Parameter p = method.getParameters()[i];
                parameters[i] = new Parameter(makeClassSafe(p.getType()), p.getName());
                parameters[i].getType().setGenericsTypes(p.getType().getGenericsTypes());
                variables.add(var(p.getName()));
            }
            ClassNode returnType = makeClassSafe(method.getReturnType());
            returnType.setGenericsTypes(method.getReturnType().getGenericsTypes());
            returnType.setGenericsPlaceHolder(method.getReturnType().isGenericsPlaceHolder());

            MethodNode newMethod = new MethodNode(
                method.getName(),
                ACC_PUBLIC,
                returnType,
                parameters,
                NO_EXCEPTIONS,
                returns(call(
                    field(providerField),
                    method.getName(),
                    args(variables)))
            );
            newMethod.setGenericsTypes(method.getGenericsTypes());
            injectMethod(declaringClass, newMethod);
        }
    }

    private static Expression defaultActivejdbcProviderInstance() {
        return call(DEFAULT_ACTIVEJDBC_PROVIDER_CNODE, "getInstance", NO_ARGS);
    }
}
