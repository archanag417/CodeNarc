/*
 * Copyright 2008 the original author or authors.
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
package org.codenarc.results

import org.codenarc.rule.Violation

/**
 * Represents the results for a directory
 *
 * @author Chris Mair
 */
class DirectoryResults implements Results {

    private final String path
    private final List children = []
    int numberOfFilesInThisDirectory = 0

    /**
     * Create a new uninitialized instance
     */
    DirectoryResults() {
    }

    /**
     * Create a new instance with the specified path
     * @param path - the path
     */
    DirectoryResults(String path) {
        this.path = path
    }

    /**
     * Create a new instance with the specified path and number of files in the directory
     */
    DirectoryResults(String path, int numberOfFilesInThisDirectory) {
        this.path = path
        this.numberOfFilesInThisDirectory = numberOfFilesInThisDirectory
    }

    /**
     * @return the path to the file or directory associated with these results
     */
    @Override
    String getPath() {
        path
    }

    void addChild(Results child) {
        children.add(child)
    }

    /**
     * Add file results to current DirectoryResult instance, or recursively in a child Directory result
     * Child DirectoryResults are created if not existing
    */
    void addFileResultRecursive(FileResults fileRes) {
        String fileResPath = new File(fileRes.path).getParent()
        if (getPath() == fileResPath || (getPath() == '' && fileResPath == null)) {
            // Same directory: Add FileResults here
            this.addChild(fileRes)
            this.numberOfFilesInThisDirectory++
        }
        else {
            // Find if there is already a child directory result
            DirectoryResults subDirResults = null
            for (def childResult in getChildren()) {
                if (childResult.getPath() == fileResPath) {
                    subDirResults = childResult
                    break
                }
            }
            // Create sub directory result if not existing
            if (subDirResults == null) {
                subDirResults = new DirectoryResults(fileResPath)
                addChild(subDirResults)
            }
            // Add FileResults in sub directory results
            subDirResults.addFileResultRecursive(fileRes)
        }
    }

    /**
     * @return the List of child Results objects; may be empty
     */
    @Override
    List getChildren() {
        children
    }

    /**
     * @return the List of all violations; may be empty
     */
    @Override
    List getViolations() {
        children.inject([]) { violations, child -> violations.addAll(child.getViolations()); violations }
    }

    @Override
    void removeViolation(Violation v) {
        children.each { child -> child.removeViolation(v) }
    }

    /**
     * Return the number of violations with the specified priority
     * @param recursive - true if the returned count should include subdirectories as well; defaults to true
     * @return the number of violations with the specified priority
     */
    int getNumberOfViolationsWithPriority(int priority, boolean recursive=true) {
        children.sum(0) { child ->
            (recursive || child.isFile()) ? child.getNumberOfViolationsWithPriority(priority) : 0
        }
    }

    /**
     * Return the number of files with violations
     * @param maxPriority - the maximum priority level; ignore violations with priority greater than this
     * @param recursive - true if the returned count should include subdirectories as well; defaults to true
     * @return the number of files containing violations
     */
    int getNumberOfFilesWithViolations(int maxPriority, boolean recursive=true) {
        children.sum(0) { child ->
            (recursive || child.isFile()) ? child.getNumberOfFilesWithViolations(maxPriority) : 0
        }
    }

    /**
     * Return the total number of (Groovy) files analyzed
     * @param recursive - true if the returned count should include subdirectories as well
     * @return the total number of files (with or without violations)
     */
    int getTotalNumberOfFiles(boolean recursive=true) {
        def total = numberOfFilesInThisDirectory
        if (recursive) {
            total += children.sum(0) { child -> child.isFile() ? 0 : child.getTotalNumberOfFiles(true) }
        }
        total
    }

    /**
     * @return false (this object does not represents the results for a single file)
     */
    @Override
    boolean isFile() {
        false
    }

    /**
     * Return the Results object with the specified path within this directory or its descendents.
     * @param path - the path to search for
     * @return this Results object if a match is found, otherwise null
     */
    @Override
    Results findResultsForPath(String path) {
        if (this.path == path) {
            return this
        }
        for (child in this.children) {
            def foundResults = child.findResultsForPath(path)
            if (foundResults) {
                return foundResults
            }
        }
        null
    }

    @Override
    String toString() {
        "DirectoryResults($path) $children"
    }

}
