/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.messages.RemovedImage;
import com.spotify.docker.client.shaded.javax.ws.rs.NotFoundException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.spotify.docker.Utils.parseImageName;

/**
 * Removes a docker image.
 */
@Mojo(name = "removeImage")
public class RemoveImageMojo extends AbstractDockerMojo {

  /**
   * Name of image to remove.
   */
  @Parameter(property = "imageName", required = true)
  private String imageName;

  /**
   * Additional tags to tag the image with.
   */
  @Parameter(property = "dockerImageTags")
  private List<String> imageTags;

  protected void execute(final DockerClient docker)
      throws MojoExecutionException, DockerException, IOException, InterruptedException {
    final String imageNameWithoutTag = parseImageName(imageName)[0];
    if (imageTags == null) {
      imageTags = Collections.singletonList("");
    }

    for (final String imageTag : imageTags) {
      final String currImageName = imageNameWithoutTag +
                             ((isNullOrEmpty(imageTag)) ? "" : (":" + imageTag));
      getLog().info("Removing -f " + currImageName);

      try {
        // force the image to be removed but don't remove untagged parents
        for (final RemovedImage removedImage : docker.removeImage(currImageName, true, false)) {
          getLog().info("Removed: " + removedImage.imageId());
        }
      } catch (ImageNotFoundException | NotFoundException e) {
        // ignoring 404 errors only
        getLog().warn("Image " + imageName + " doesn't exist and cannot be deleted - ignoring");
      }
    }
  }
}
