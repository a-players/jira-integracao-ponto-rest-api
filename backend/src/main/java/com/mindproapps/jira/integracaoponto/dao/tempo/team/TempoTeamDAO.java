package com.mindproapps.jira.integracaoponto.dao.tempo.team;

import com.mindproapps.jira.integracaoponto.dao.base.BaseDAO;
import com.mindproapps.jira.integracaoponto.util.LegacySQLProcessor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;

@Log4j
@Component
public class TempoTeamDAO extends BaseDAO {

    private Boolean isUserKeyTeamLeadOrHasTimesheetsPermissions(String userKey, Integer teamId, String permission) {
        log.info("isUserKeyTeamLeadOrHasTimesheetsPermissions: userKey = " + userKey + ", teamId = " + teamId + ", permission = " + permission);
        try {
            LegacySQLProcessor sqlProcessor = this.createSQLProcessor();
            String sql = "SELECT COUNT(1) FROM \"AO_AEFED0_TEAM_V2\" aatv WHERE (aatv.\"LEAD\" = ? " +
                    "OR EXISTS(SELECT 1 FROM \"AO_AEFED0_PGP_MEMBER_V2\" aapmv, \"AO_AEFED0_PGP_GROUP_TO_TEAM\" aappgt  \n" +
                    "WHERE EXISTS (\n" +
                    "SELECT 1 FROM \"AO_AEFED0_PGP_PERMISSION_V2\" aappv \n" +
                    "WHERE aappv.\"PERMISSION_GROUP_ID\" = aapmv.\"PERMISSION_GROUP_ID\" \n" +
                    "AND aappv.\"KEY\" = '" + permission + "'\n" +
                    ")\n" +
                    "AND aappgt.\"PERMISSION_GROUP_ID\" = aapmv.\"PERMISSION_GROUP_ID\" \n" +
                    "AND aatv.\"ID\" = aappgt.\"TEAM_ID\" \n" +
                    "AND aapmv.\"KEY\" = ?))\n" +
                    ((teamId != null) ? "AND aatv.\"ID\" = " + teamId : "");
            sqlProcessor.prepareStatement(sql);
            sqlProcessor.setValue(userKey);
            sqlProcessor.setValue(userKey);
            ResultSet result = sqlProcessor.executeQuery();
            if (result.next() && result.getInt(1) > 0) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } catch (Exception e) {
            log.error("DAO: isUserKeyTeamLead: " + e.getMessage(), e);
        }
        return Boolean.FALSE;
    }

    public Boolean isUserKeyTeamLeadOrHasViewTimesheetsPermissions(String userKey, Integer teamId) {
        log.info("isUserKeyTeamLeadOrHasViewTimesheetsPermissions: userKey = " + userKey + ", teamId = " + teamId);
        return this.isUserKeyTeamLeadOrHasTimesheetsPermissions(userKey, teamId, "permissions.worklog.view");
    }

    public Boolean isUserKeyTeamLeadOrHasApproveTimesheetsPermissions(String userKey, Integer teamId) {
        log.info("isUserKeyTeamLeadOrHasApproveTimesheetsPermissions: userKey = " + userKey + ", teamId = " + teamId);
        return this.isUserKeyTeamLeadOrHasTimesheetsPermissions(userKey, teamId, "permissions.worklog.approve");
    }

    public Long userWorkLoad(String userKey, String startDate, String endDate) {
        log.info("userWorkLoad: userKey = " + userKey + ", startDate = " + startDate + ", endDate = " + endDate);
        try {
            LegacySQLProcessor sqlProcessor = this.createSQLProcessor();
            String sql = "SELECT SUM(wl.tt) AS \"WORK_LOAD\" \n" +
                    "FROM (SELECT CASE WHEN wsd.\"REQUIRED_SECONDS\" - COALESCE(hsd.\"DURATION_SECONDS\", 0) < 0 THEN 0 ELSE wsd.\"REQUIRED_SECONDS\" - COALESCE(hsd.\"DURATION_SECONDS\", 0) END AS tt \n" +
                    "      FROM generate_series(?::timestamp, ?::timestamp, '1 day'::interval) dd \n" +
                    "      JOIN \"AO_013613_WL_SCHEME_MEMBER\" wsm ON \"MEMBER_KEY\" = ? \n" +
                    "      JOIN \"AO_013613_WL_SCHEME_DAY\" wsd ON wsd.\"WORKLOAD_SCHEME_ID\" = wsm.\"WORKLOAD_SCHEME_ID\" AND wsd.\"DAY\" = EXTRACT(isodow from dd) \n" +
                    "      LEFT JOIN \"AO_013613_HD_SCHEME_MEMBER\" hsm ON hsm.\"USER_KEY\" = wsm.\"MEMBER_KEY\" \n" +
                    "      LEFT JOIN \"AO_013613_HD_SCHEME_DAY\" hsd ON hsd.\"HOLIDAY_SCHEME_ID\" = hsm.\"HOLIDAY_SCHEME_ID\" AND (hsd.\"DATE\" = dd AND \"TYPE\" = 'floating' OR TO_CHAR(hsd.\"DATE\", 'MM-DD') = TO_CHAR(dd, 'MM-DD') AND \"TYPE\" = 'fixed')) AS wl";
            sqlProcessor.prepareStatement(sql);
            sqlProcessor.setValue(startDate);
            sqlProcessor.setValue(endDate);
            sqlProcessor.setValue(userKey);
            ResultSet result = sqlProcessor.executeQuery();
            if (result.next() && result.getLong("WORK_LOAD") > 0) {
                return result.getLong("WORK_LOAD");
            }
        } catch (Exception e) {
            log.error("DAO: userWorkLoad: " + e.getMessage(), e);
        }
        return 0L;
    }
}
