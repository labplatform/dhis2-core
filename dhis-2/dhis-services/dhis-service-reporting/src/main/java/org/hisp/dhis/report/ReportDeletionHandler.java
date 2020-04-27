package org.hisp.dhis.report;

/*
 * Copyright (c) 2004-2020, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import static com.google.common.base.Preconditions.checkNotNull;

import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.system.deletion.DeletionHandler;
import org.hisp.dhis.visualization.Visualization;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
@Component( "org.hisp.dhis.report.ReportDeletionHandler" )
public class ReportDeletionHandler
    extends
    DeletionHandler
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private final ReportService reportService;

    private final JdbcTemplate jdbcTemplate;

    public ReportDeletionHandler( ReportService reportService, final JdbcTemplate jdbcTemplate )
    {
        checkNotNull( reportService );
        checkNotNull( jdbcTemplate );
        this.reportService = reportService;
        this.jdbcTemplate = jdbcTemplate;
    }

    // -------------------------------------------------------------------------
    // DeletionHandler implementation
    // -------------------------------------------------------------------------

    @Override
    public String getClassName()
    {
        return Visualization.class.getSimpleName();
    }

    @Override
    public String allowDeleteVisualization( Visualization visualization )
    {
        for ( Report report : reportService.getAllReports() )
        {
            if ( report.getVisualization() != null && report.getVisualization().equals( visualization ) )
            {
                return report.getName();
            }
        }

        return null;
    }

    @Override
    public String allowDeleteOrganisationUnitGroupSet( OrganisationUnitGroupSet groupSet )
    {
        String sql = "select count(*) "
            + " from orgunitgroupsetdimension d JOIN reporttable_orgunitgroupsetdimensions r "
            + " ON d.orgunitgroupsetdimensionid = r.orgunitgroupsetdimensionid " + " WHERE d.orgunitgroupsetid = "
            + groupSet.getId();

        return jdbcTemplate.queryForObject( sql, Integer.class ) == 0 ? null : "orgunitgroupsetdimension";
    }

    @Override
    public String allowDeleteOrganisationUnitGroup( OrganisationUnitGroup group )
    {
        String sql = "select count(*) "
            + " from orgunitgroupsetdimension d JOIN reporttable_orgunitgroupsetdimensions r "
            + " ON d.orgunitgroupsetdimensionid = r.orgunitgroupsetdimensionid "
            + " JOIN orgunitgroupsetdimension_items i ON d.orgunitgroupsetdimensionid = i.orgunitgroupsetdimensionid "
            + " WHERE i.orgunitgroupid = " + group.getId();

        return jdbcTemplate.queryForObject( sql, Integer.class ) == 0 ? null : "orgunitgroupsetdimension_items";
    }
}
