package com.redhat.cases.mcp;

import com.redhat.cases.service.SupportCaseService;

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

            - View all your cases: `searchCases`
            - Get statistics: `getStatistics`
            - Create new case: `createCase`

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

            ## Phase 3: Escalate to Red Hat Support

            ### When to Escalate

            - Problem persists after troubleshooting
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
}
