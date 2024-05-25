package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.algolia.search.DefaultSearchClient;
import com.algolia.search.SearchClient;
import com.algolia.search.SearchIndex;
import com.algolia.search.iterators.SynonymsIterable;
import com.algolia.search.iterators.RulesIterable;
import com.algolia.search.models.settings.IndexSettings;
import com.algolia.search.models.synonyms.Synonym;
import com.algolia.search.models.rules.Rule;


import com.algolia.search.models.synonyms.SaveSynonymResponse;
import com.algolia.search.models.rules.SaveRuleResponse;

public class AlgoliaCopyConfig {

    private static final Logger logger = Logger.getLogger(AlgoliaCopyConfig.class.getName());

    public static void main(String[] args) throws IOException {
        // Initialize Algolia clients for source and destination environments
        SearchClient sourceClient = DefaultSearchClient.create("SourceAppID","SourceApiKey" ); // These values will be taken from aws secret manager
        SearchClient destinationClient = DefaultSearchClient.create("DestinationAppID", "DestinationApiKey"); // These values will be taken from aws secret manager

        // Specify the indices to copy from and to
        String sourceIndexName = "global_product_index";
        String destinationIndexName = "global_product_index";

        // Get the source and destination indices
        SearchIndex sourceIndex = sourceClient.initIndex(sourceIndexName);
        SearchIndex destinationIndex = destinationClient.initIndex(destinationIndexName);

        try {
            // Copy the configuration settings
            IndexSettings sourceSettings = sourceIndex.getSettings();
            destinationIndex.setSettings(sourceSettings).waitTask();
            logger.log(Level.INFO, "Configuration settings copied successfully.");

            // Copy the synonyms
            List<Synonym> synonyms = new ArrayList<>();
            SynonymsIterable synonymsIterate = sourceIndex.browseSynonyms();
            synonymsIterate.forEach(synonyms::add);
            if(!synonyms.isEmpty()) {
                destinationIndex.saveSynonyms(synonyms).waitTask();
                logger.log(Level.INFO, "Synonyms copied successfully.");
            }

            // Copy the rules
            List<Rule> rules = new ArrayList<>();
            RulesIterable rulesIterate = sourceIndex.browseRules();
            rulesIterate.forEach(rules::add);
            if(!rules.isEmpty()) {
                destinationIndex.saveRules(rules);
                logger.log(Level.INFO, "Rules copied successfully.");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error copying data: " + e.getMessage(), e);
        } finally {
            // Close the clients to release resources
            sourceClient.close();
            destinationClient.close();
        }
    }
}
