/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cosmosdb.samples;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountKind;
import com.microsoft.azure.management.samples.Utils;

import java.io.File;

/**
 * Azure CosmosDB sample for high availability.
 *  - Create a CosmosDB configured with IP range filterR
 *  - Delete the CosmosDB.
 */
public final class CreateCosmosDBWithIPRange {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure resource client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String docDBName = Utils.createRandomName("docDb", 10);
        final String rgName = Utils.createRandomName("rgNEMV", 24);

        try {
            //============================================================
            // Create a CosmosDB

            System.out.println("Creating a CosmosDB...");
            CosmosDBAccount cosmosDBAccount = azureResourceManager
                    .cosmosDBAccounts()
                    .define(docDBName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
                    .withSessionConsistency()
                    .withWriteReplication(Region.US_WEST)
                    .withReadReplication(Region.US_CENTRAL)
                    .withIpRangeFilter("13.91.6.132,13.91.6.1/24")
                    .create();

            System.out.println("Created CosmosDB");
            Utils.print(cosmosDBAccount);

            //============================================================
            // Delete CosmosDB
            System.out.println("Deleting the CosmosDB");
            azureResourceManager.cosmosDBAccounts().deleteById(cosmosDBAccount.id());
            System.out.println("Deleted the CosmosDB");

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                System.out.println("Deleting resource group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted resource group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
            final String azureClientId = System.getenv("AZURE_CLIENT_ID");
            final String azureTenantId = System.getenv("AZURE_TENANT_ID");
            final String azureSubscriptionId = System.getenv("AZURE_SUBSCRIPTION_ID");
            final AzureResourceManager azureResourceManager =
                    AzureResourceManager.configure()
                            .withLogLevel(HttpLogDetailLevel.BASIC)
                            .authenticate(new ClientCertificateCredentialBuilder()
                                            .clientId(azureClientId)
                                            .tenantId(azureTenantId)
                                            .pemCertificate(credFile.getPath())
                                            .build(),
                                    new AzureProfile(azureTenantId,
                                            azureSubscriptionId, AzureEnvironment.AZURE))
                            .withSubscription(azureSubscriptionId);

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private CreateCosmosDBWithIPRange() {
    }
}
