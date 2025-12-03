package com.redhat.cases.adapter.mcp;

import com.redhat.cases.application.service.SupportCaseService;

import io.quarkiverse.mcp.server.Prompt;
import io.quarkiverse.mcp.server.PromptArg;
import io.quarkiverse.mcp.server.PromptMessage;
import io.quarkiverse.mcp.server.TextContent;

import jakarta.inject.Inject;

import java.util.List;

/**
 * MCP Prompts for Red Hat Support - Conversational guides and workflows.
 *
 * Available prompts:
 * - createCaseGuide: Step-by-step guide to create a support case
 * - troubleshootingGuide: Guide to diagnose and manage existing cases
 * - clusterDiagnosticGuide: OpenShift cluster diagnostics with escalation workflow
 * - executiveSummary: Get an overview of all support cases
 * - knowledgeBaseSearchGuide: Tips for effective KB searches
 * - fullDiagnosticWorkflow: Complete workflow: KB search -> diagnose -> escalate
 */
public class SupportCasePrompts {

    @Inject
    SupportCaseService caseService;

    @Prompt(description = "Interactive guide to create a new Red Hat support case with all required information")
    PromptMessage createCaseGuide(
            @PromptArg(description = "Red Hat product (e.g. OpenShift, RHEL)", defaultValue = "OpenShift") String product) {

        List<String> products = caseService.getProducts();
        String productList = String.join(", ", products);

        String guide = String.format("""
            # Guide: Create a Red Hat Support Case

            ## Product: %s

            ### Required Information

            1. **Title** (max 255 characters)
               Brief description of the problem

            2. **Description** (detailed)
               - What were you doing when the problem occurred?
               - Exact error message (copy/paste if possible)
               - Steps to reproduce
               - Business impact

            3. **Product & Version**
               - Use `listProducts` to see available products
               - Use `listVersions` to see versions for your product

            4. **Priority**
               | Level | When to use |
               |-------|-------------|
               | URGENT | Production system completely down |
               | HIGH | Critical functionality severely affected |
               | NORMAL | Problem affecting operations |
               | LOW | Questions or minor issues |

            ### Steps to Create

            ```
            1. listProducts                           # Find your product
            2. listVersions productCode='%s'          # Get available versions
            3. createCase title='...' description='...' product='%s' version='...' priority='NORMAL'
            ```

            ### Example

            ```
            createCase
              title="OAuth authentication failing after upgrade"
              description="After upgrading to 4.14.5, users cannot login via LDAP.
                          Error: 'invalid_grant: Token is not active'
                          Affects all 500 users in production cluster.
                          Started: 2024-01-15 10:00 UTC"
              product="OpenShift Container Platform"
              version="4.14"
              priority="HIGH"
            ```

            ### Available Products
            %s

            Ready to create your case? Start with `listProducts` to verify the product name.
            """, product, product, product, productList);

        return PromptMessage.withUserRole(new TextContent(guide));
    }

    @Prompt(description = "Guide to diagnose, update and manage an existing support case")
    PromptMessage troubleshootingGuide(
            @PromptArg(description = "Case number to troubleshoot") String caseNumber) {

        String guide = String.format("""
            # Guide: Manage Support Case %s

            ## Step 1: Review Current Status

            ```
            getCase caseNumber='%s'
            ```

            This shows: status, priority, description, comments history, and assigned contact.

            ## Step 2: Available Actions

            ### Add Information
            Provide logs, screenshots, or additional details:
            ```
            addComment caseNumber='%s' comment='...' author='your-name'
            ```

            ### Change Status
            Update the case status:
            ```
            updateCase caseNumber='%s' status='Waiting on Customer'
            ```

            Valid statuses:
            - `Waiting on Red Hat` - Waiting for support team action
            - `Waiting on Customer` - Support is waiting for your response
            - `Closed` - Issue resolved

            ### Reassign Contact
            Change the primary contact:
            ```
            updateCase caseNumber='%s' contactSSOName='new-user-sso'
            ```

            ### Reopen Closed Case
            If the issue recurs:
            ```
            updateCase caseNumber='%s' status='Waiting on Red Hat'
            ```

            ## Best Practices

            - Respond to support requests within 24 hours
            - Include relevant logs with each update
            - Update the case if the problem changes
            - Close the case when resolved to help metrics

            ## Need More Help?

            - Search Knowledge Base: `searchKnowledgeBase query='your error'`
            - View all your cases: `searchCases`
            - Get statistics: `getStatistics`

            What would you like to do with case %s?
            """, caseNumber, caseNumber, caseNumber, caseNumber, caseNumber, caseNumber, caseNumber);

        return PromptMessage.withUserRole(new TextContent(guide));
    }

    @Prompt(description = "Comprehensive guide for diagnosing OpenShift cluster issues and escalating to Red Hat support when needed")
    PromptMessage clusterDiagnosticGuide(
            @PromptArg(description = "Cluster name or identifier", defaultValue = "production") String clusterName) {

        String guide = String.format("""
            # OpenShift Cluster Diagnostic Guide: %s

            ## Phase 1: Initial Assessment

            Use Kubernetes MCP tools to gather information:

            ```
            1. mcp_kubernetes_events_list        # Recent cluster events
            2. mcp_kubernetes_nodes_top          # Node resource usage
            3. mcp_kubernetes_pods_list          # All pods status
            ```

            ## Phase 2: Identify Problems

            ### Common Pod Issues

            | Status | Likely Cause | Diagnostic |
            |--------|--------------|------------|
            | CrashLoopBackOff | App error, missing config | Check pod logs |
            | ImagePullBackOff | Registry auth, image not found | Verify image and pull secret |
            | Pending | No resources, node selector | Check node capacity |
            | OOMKilled | Memory limit exceeded | Increase memory limits |

            For problematic pods:
            ```
            mcp_kubernetes_pods_log name='pod-name' namespace='namespace'
            mcp_kubernetes_pods_get name='pod-name' namespace='namespace'
            ```

            ### Node Issues

            For nodes showing problems:
            ```
            mcp_kubernetes_nodes_log name='node-name' query='kubelet'
            mcp_kubernetes_nodes_stats_summary name='node-name'
            ```

            ## Phase 3: Search for Solutions

            Before escalating, search the Knowledge Base:
            ```
            searchKnowledgeBase query='CrashLoopBackOff openshift 4' documentType='Solution'
            ```

            If you find a relevant article:
            ```
            getSolution solutionId='article-id'
            ```

            ## Phase 4: Escalate to Red Hat Support

            ### When to Escalate

            - Problem persists after trying KB solutions
            - Suspected product bug
            - Need Red Hat engineering assistance
            - Production impact requires urgent support

            ### Create Support Case

            ```
            createCase
              title="[%s] Brief problem description"
              description="
                Cluster: %s
                OpenShift Version: X.Y.Z

                PROBLEM:
                - What is happening
                - When it started

                SYMPTOMS:
                - Error messages
                - Affected components

                ATTEMPTED:
                - Solutions tried from KB
                - Other troubleshooting steps

                IMPACT:
                - Business impact
                - Number of users affected
              "
              product="OpenShift Container Platform"
              version="4.14"
              priority="HIGH"
            ```

            ### Priority Guide

            | Priority | Situation |
            |----------|-----------|
            | URGENT | Cluster down, no workaround |
            | HIGH | Major feature broken, production affected |
            | NORMAL | Issue affecting operations, workaround exists |
            | LOW | Questions, minor issues |

            ## Quick Reference

            - List cases: `searchCases`
            - Case details: `getCase caseNumber='...'`
            - Add info: `addComment caseNumber='...' comment='...' author='...'`
            - Statistics: `getStatistics`

            What issue are you experiencing with cluster %s?
            """, clusterName, clusterName, clusterName, clusterName);

        return PromptMessage.withUserRole(new TextContent(guide));
    }

    @Prompt(description = "Get an executive summary of all support cases with statistics and key metrics")
    PromptMessage executiveSummary() {
        String guide = """
            # Executive Summary: Support Cases

            ## Get Overview

            ### 1. Statistics Dashboard
            ```
            getStatistics
            ```
            Shows: total cases, urgent count, distribution by status/priority/product

            ### 2. Urgent Cases (Immediate Attention)
            ```
            searchCases priority='URGENT'
            ```

            ### 3. All Active Cases
            ```
            searchCases
            ```

            ### 4. All Cases Including Closed
            ```
            searchCases includeClosed=true
            ```

            ### 5. Cases by Product
            ```
            searchCases product='OpenShift'
            searchCases product='RHEL'
            ```

            ### 6. Cases Waiting on Customer
            ```
            searchCases status='Waiting on Customer'
            ```

            ## Key Metrics to Monitor

            | Metric | Why it matters |
            |--------|---------------|
            | Urgent open cases | Immediate business risk |
            | Waiting on Customer | Cases needing your response |
            | Cases by product | Resource allocation |
            | Total open vs closed | Support load trend |

            ## Recommended Actions

            1. Review urgent cases daily
            2. Respond to "Waiting on Customer" within 24h
            3. Close resolved cases promptly
            4. Track trends weekly

            What information would you like to see first?
            """;

        return PromptMessage.withUserRole(new TextContent(guide));
    }

    @Prompt(description = "Tips and techniques for effective Red Hat Knowledge Base searches")
    PromptMessage knowledgeBaseSearchGuide(
            @PromptArg(description = "Problem or error to search for", defaultValue = "") String problem) {

        String problemDisplay = problem.isBlank() ? "(not specified yet)" : problem;

        String guide = String.format("""
            # Guide: Search Red Hat Knowledge Base

            ## Your Problem: %s

            ## Search Techniques

            ### 1. Use Error Messages Directly

            Copy the exact error message:
            ```
            searchKnowledgeBase query='error: connection refused to database:5432'
            ```

            ### 2. Combine Keywords

            | Problem | Effective Search |
            |---------|-----------------|
            | Pod won't start | `CrashLoopBackOff pod openshift` |
            | Memory issues | `OOMKilled container memory limit` |
            | Auth problems | `oauth authentication error 401` |
            | Cluster unresponsive | `apiserver timeout openshift` |
            | Certificate errors | `certificate expired x509 openshift` |
            | Storage issues | `PVC pending mount volume` |

            ### 3. Filter by Document Type

            ```
            # Proven solutions with steps
            searchKnowledgeBase query='...' documentType='Solution'

            # Official documentation
            searchKnowledgeBase query='...' documentType='Documentation'

            # Technical articles
            searchKnowledgeBase query='...' documentType='Article'
            ```

            ### 4. Filter by Product

            ```
            searchKnowledgeBase query='...' product='OpenShift'
            searchKnowledgeBase query='...' product='RHEL'
            ```

            ## Workflow

            ```
            1. searchKnowledgeBase query='your error message' documentType='Solution'
            2. Review results - note the ID of relevant articles
            3. getSolution solutionId='article-id'
            4. Follow the resolution steps
            5. If no solution found: createCase
            ```

            ## Example

            ```
            # Search for CrashLoopBackOff solutions
            searchKnowledgeBase query='CrashLoopBackOff openshift 4.14' documentType='Solution' maxResults=5

            # Get full solution
            getSolution solutionId='5049001'
            ```

            ## No Results?

            - Try fewer, more generic keywords
            - Remove version numbers
            - Use the core error message only
            - If still nothing: create a support case

            What error or problem do you want to search for?
            """, problemDisplay);

        return PromptMessage.withUserRole(new TextContent(guide));
    }

    @Prompt(description = "Complete workflow: search Knowledge Base first, then diagnose, and escalate to support if needed")
    PromptMessage fullDiagnosticWorkflow(
            @PromptArg(description = "Problem description") String problemDescription,
            @PromptArg(description = "Affected product", defaultValue = "OpenShift") String product) {

        String guide = String.format("""
            # Complete Diagnostic Workflow

            ## Problem: %s
            ## Product: %s

            ---

            ## Phase 1: Search Existing Solutions

            ### Step 1.1: Search Knowledge Base
            ```
            searchKnowledgeBase query='%s' product='%s' documentType='Solution'
            ```

            ### Step 1.2: Review and Apply
            If relevant results found:
            ```
            getSolution solutionId='...'
            ```

            Follow the diagnostic and resolution steps.

            ---

            ## Phase 2: No Solution Found? Create Case

            ### Step 2.1: Gather Information

            Before creating a case, collect:
            - Exact product version
            - Complete error messages
            - Relevant logs
            - Steps to reproduce
            - Business impact

            ### Step 2.2: Verify Product and Version
            ```
            listProducts
            listVersions productCode='%s'
            ```

            ### Step 2.3: Create Support Case
            ```
            createCase
              title="Brief description of %s"
              description="
                Product: %s
                Version: [from listVersions]

                PROBLEM:
                %s

                ERROR MESSAGES:
                [paste exact errors]

                LOGS:
                [relevant log excerpts]

                STEPS TO REPRODUCE:
                1. ...
                2. ...

                IMPACT:
                [business impact, users affected]

                ATTEMPTED SOLUTIONS:
                - Searched KB, no applicable solution found
                - [other steps tried]
              "
              product="%s"
              version="..."
              priority="NORMAL"
            ```

            ---

            ## Phase 3: Track and Update

            ### Monitor Case
            ```
            getCase caseNumber='...'
            ```

            ### Add Information
            ```
            addComment caseNumber='...' comment='...' author='...'
            ```

            ### View All Cases
            ```
            searchCases
            ```

            ---

            ## Phase 4: Escalation (if critical)

            For URGENT priority cases:
            1. Create case with priority='URGENT'
            2. Call Red Hat 24x7 support line
            3. Reference the case number

            ---

            Ready to start? Let's search the Knowledge Base first.
            """, problemDescription, product, problemDescription, product, product,
                problemDescription, product, problemDescription, product);

        return PromptMessage.withUserRole(new TextContent(guide));
    }
}
