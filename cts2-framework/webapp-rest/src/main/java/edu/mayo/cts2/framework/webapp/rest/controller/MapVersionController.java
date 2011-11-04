/*
 * Copyright: (c) 2004-2011 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
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
package edu.mayo.cts2.framework.webapp.rest.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import edu.mayo.cts2.framework.model.command.Page;
import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.core.Message;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.mapversion.MapVersion;
import edu.mayo.cts2.framework.model.mapversion.MapVersionDirectory;
import edu.mayo.cts2.framework.model.mapversion.MapVersionDirectoryEntry;
import edu.mayo.cts2.framework.model.mapversion.MapVersionMsg;
import edu.mayo.cts2.framework.model.service.core.Query;
import edu.mayo.cts2.framework.model.service.exception.UnknownMapVersion;
import edu.mayo.cts2.framework.model.updates.ChangeableResourceChoice;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.service.command.restriction.MapVersionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.profile.mapversion.MapVersionMaintenanceService;
import edu.mayo.cts2.framework.service.profile.mapversion.MapVersionQueryService;
import edu.mayo.cts2.framework.service.profile.mapversion.MapVersionReadService;
import edu.mayo.cts2.framework.webapp.rest.command.QueryControl;
import edu.mayo.cts2.framework.webapp.rest.command.RestFilter;
import edu.mayo.cts2.framework.webapp.rest.command.RestReadContext;

/**
 * The Class MapVersionController.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Controller
public class MapVersionController extends AbstractServiceAwareController {

	@Cts2Service
	private MapVersionReadService mapVersionReadService;
	
	@Cts2Service
	private MapVersionQueryService mapVersionQueryService;
	
	@Cts2Service
	private MapVersionMaintenanceService mapVersionMaintenanceService;
	
	private static UrlTemplateBinder<MapVersion> URL_BINDER = new 
			UrlTemplateBinder<MapVersion>(){

		@Override
		public String getValueForPathAttribute(String attribute, MapVersion resource) {
			if(attribute.equals(VAR_MAPVERSIONID)){
				return resource.getMapVersionName();
			}
			if(attribute.equals(VAR_MAPID)){
				return resource.getVersionOf().getContent();
			}
			return null;
		}

	};
	
	private final static MessageFactory<MapVersion> MESSAGE_FACTORY = 
			new MessageFactory<MapVersion>() {

		@Override
		public Message createMessage(MapVersion resource) {
			MapVersionMsg msg = new MapVersionMsg();
			msg.setMapVersion(resource);

			return msg;
		}
	};
	
	/**
	 * Creates the map version.
	 *
	 * @param httpServletRequest the http servlet request
	 * @param changeseturi the changeseturi
	 * @param mapVersion the map version
	 * @param mapName the map name
	 * @param mapVersionName the map version name
	 */
	@RequestMapping(value=PATH_MAPVERSION_OF_MAP_BYID, method=RequestMethod.PUT)
	@ResponseBody
	public void updateMapVersion(
			HttpServletRequest httpServletRequest,
			@RequestParam(required=false) String changeseturi,
			@RequestBody MapVersion mapVersion,
			@PathVariable(VAR_MAPID) String mapName,
			@PathVariable(VAR_MAPVERSIONID) String mapVersionName) {
			
		ChangeableResourceChoice choice = new ChangeableResourceChoice();
		choice.setMapVersion(mapVersion);
		
		this.getUpdateHandler().update(
				choice, 
				changeseturi,
				ModelUtils.nameOrUriFromName(mapVersionName), 
				this.mapVersionMaintenanceService);
	}
	
	@RequestMapping(value=PATH_MAPVERSION, method=RequestMethod.POST)
	@ResponseBody
	public void createMapVersion(
			HttpServletRequest httpServletRequest,
			@RequestParam(required=false) String changeseturi,
			@RequestBody MapVersion mapVersion) {
			
		ChangeableResourceChoice choice = new ChangeableResourceChoice();
		choice.setMapVersion(mapVersion);
			
		this.getCreateHandler().create(
				choice,
				changeseturi,
				PATH_MAPVERSION_OF_MAP_BYID,
				URL_BINDER, 
				this.mapVersionMaintenanceService);
	}

	/**
	 * Gets the map versions of map.
	 *
	 * @param httpServletRequest the http servlet request
	 * @param restrictions the restrictions
	 * @param resolvedFilter the filter
	 * @param page the page
	 * @param mapName the map name
	 * @return the map versions of map
	 */
	@RequestMapping(value={
			PATH_MAPVERSIONS_OF_MAP}, method=RequestMethod.GET)
	@ResponseBody
	public MapVersionDirectory getMapVersionsOfMap(
			HttpServletRequest httpServletRequest,
			MapVersionQueryServiceRestrictions restrictions,
			RestFilter restFilter,
			Page page,
			@PathVariable(VAR_MAPID) String mapName) {
		
		restrictions.setMap(mapName);
		
		return this.getMapVersions(
				httpServletRequest, 
				null, 
				restrictions,
				restFilter,
				page);
	}
	
	/**
	 * Gets the map versions of map.
	 *
	 * @param httpServletRequest the http servlet request
	 * @param query the query
	 * @param restrictions the restrictions
	 * @param resolvedFilter the filter
	 * @param page the page
	 * @param mapName the map name
	 * @return the map versions of map
	 */
	@RequestMapping(value={
			PATH_MAPVERSIONS_OF_MAP}, method=RequestMethod.POST)
	@ResponseBody
	public MapVersionDirectory getMapVersionsOfMap(
			HttpServletRequest httpServletRequest,
			@RequestBody Query query,
			MapVersionQueryServiceRestrictions restrictions,
			RestFilter restFilter,
			Page page,
			@PathVariable(VAR_MAPID) String mapName) {
		
		restrictions.setMap(mapName);
		
		return this.getMapVersions(
				httpServletRequest,
				null, 
				restrictions, 
				restFilter,
				page);
	}
	
	/**
	 * Gets the map versions.
	 *
	 * @param httpServletRequest the http servlet request
	 * @param restrictions the restrictions
	 * @param resolvedFilter the filter
	 * @param page the page
	 * @return the map versions
	 */
	@RequestMapping(value={
			PATH_MAPVERSIONS_OF_MAP}, method=RequestMethod.GET)
	@ResponseBody
	public MapVersionDirectory getMapVersions(
			HttpServletRequest httpServletRequest,
			MapVersionQueryServiceRestrictions restrictions,
			RestFilter restFilter,
			Page page) {
		
		return this.getMapVersions(
				httpServletRequest, 
				null, 
				restrictions, 
				restFilter,
				page);
	}
	
	/**
	 * Gets the map versions.
	 *
	 * @param httpServletRequest the http servlet request
	 * @param query the query
	 * @param restrictions the restrictions
	 * @param resolvedFilter the filter
	 * @param page the page
	 * @return the map versions
	 */
	@RequestMapping(value={
			PATH_MAPVERSIONS_OF_MAP}, method=RequestMethod.POST)
	@ResponseBody
	public MapVersionDirectory getMapVersions(
			HttpServletRequest httpServletRequest,
			@RequestBody Query query,
			MapVersionQueryServiceRestrictions restrictions,
			RestFilter restFilter,
			Page page) {
		
		ResolvedFilter filterComponent = this.processFilter(restFilter, this.mapVersionQueryService);
		
		DirectoryResult<MapVersionDirectoryEntry> directoryResult = 
			this.mapVersionQueryService.getResourceSummaries(query, createSet(filterComponent), restrictions, null, page);
		
		MapVersionDirectory directory = this.populateDirectory(
				directoryResult, 
				page, 
				httpServletRequest, 
				MapVersionDirectory.class);
		
		return directory;
	}
	
	/**
	 * Does map version exist.
	 *
	 * @param httpServletResponse the http servlet response
	 * @param mapName the map name
	 * @param mapVersionName the map version name
	 */
	@RequestMapping(value=PATH_MAPVERSION_OF_MAP_BYID, method=RequestMethod.HEAD)
	@ResponseBody
	public void doesMapVersionExist(
			HttpServletResponse httpServletResponse,
			@PathVariable(VAR_MAPID) String mapName,
			@PathVariable(VAR_MAPVERSIONID) String mapVersionName) {
		
		this.doExists(
				httpServletResponse, 
				this.mapVersionReadService, 
				UnknownMapVersion.class, 
				ModelUtils.nameOrUriFromName(mapVersionName));
	}
	
	/**
	 * Gets the map versions of map count.
	 *
	 * @param httpServletResponse the http servlet response
	 * @param query the query
	 * @param restrictions the restrictions
	 * @param resolvedFilter the filter
	 * @param mapId the map id
	 * @return the map versions of map count
	 */
	@RequestMapping(value={
			PATH_MAPVERSIONS_OF_MAP}, method=RequestMethod.HEAD)
	@ResponseBody
	public void getMapVersionsOfMapCount(
			HttpServletResponse httpServletResponse,
			@RequestBody Query query,
			MapVersionQueryServiceRestrictions restrictions,
			RestFilter restFilter,
			@PathVariable(VAR_MAPID) String mapId) {
		
		ResolvedFilter filterComponent = this.processFilter(restFilter, this.mapVersionQueryService);
		
		int count =
			this.mapVersionQueryService.count(query, createSet(filterComponent), restrictions);
		
		this.setCount(count, httpServletResponse);
	}
	
	
	/**
	 * Gets the map versions count.
	 *
	 * @param httpServletResponse the http servlet response
	 * @param query the query
	 * @param restrictions the restrictions
	 * @param resolvedFilter the filter
	 * @param page the page
	 * @return the map versions count
	 */
	@RequestMapping(value={
			PATH_MAPVERSIONS}, method=RequestMethod.HEAD)
	@ResponseBody
	public void getMapVersionsCount(
			HttpServletResponse httpServletResponse,
			@RequestBody Query query,
			MapVersionQueryServiceRestrictions restrictions,
			RestFilter restFilter,
			Page page) {
		
		ResolvedFilter filterComponent = 
				this.processFilter(restFilter, this.mapVersionQueryService);
		
		int count =
			this.mapVersionQueryService.count(query, createSet(filterComponent), restrictions);
		
		this.setCount(count, httpServletResponse);
	}
	
	/**
	 * Gets the map version by name.
	 *
	 * @param httpServletRequest the http servlet request
	 * @param mapName the map name
	 * @param mapVersionName the map version name
	 * @return the map version by name
	 */
	@RequestMapping(value={	
			PATH_MAPVERSION_OF_MAP_BYID
			},
		method=RequestMethod.GET)
	@ResponseBody
	public Message getMapVersionByName(
			HttpServletRequest httpServletRequest,
			RestReadContext restReadContext,
			@PathVariable(VAR_MAPID) String mapName,
			@PathVariable(VAR_MAPVERSIONID) String mapVersionName) {
		
		return this.doRead(
				httpServletRequest, 
				MESSAGE_FACTORY, 
				this.mapVersionReadService, 
				restReadContext,
				UnknownMapVersion.class,
				ModelUtils.nameOrUriFromName(mapVersionName));
	}
	
	@RequestMapping(value=PATH_MAPVERSION_BYURI, method=RequestMethod.GET)
	@ResponseBody
	public ModelAndView getMapVersionByUri(
			HttpServletRequest httpServletRequest,
			QueryControl queryControl,
			@PathVariable(VAR_URI) String uri,
			@RequestParam(value="redirect", defaultValue="false") boolean redirect) {
		
		return this.doReadByUri(
				httpServletRequest, 
				MESSAGE_FACTORY, 
				PATH_MAP_BYURI, 
				PATH_MAP_BYID, 
				URL_BINDER, 
				this.mapVersionReadService,
				ModelUtils.nameOrUriFromUri(uri),
				redirect);
	}
}