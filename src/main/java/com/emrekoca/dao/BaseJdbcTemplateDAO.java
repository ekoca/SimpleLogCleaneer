package com.emrekoca.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Base class for DAOs with some utilities.
 * 
 * When adding methods to this class keep it as similar as possible to its
 * origin, JdbcTemplate, including error handling. Since this class is generic
 * and used by many packages, it should be minimal and not contain any data
 * specific code.
 * 
 * @author Emre Koca
 */
@Component
public abstract class BaseJdbcTemplateDAO {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * Return a single row as an object. In case of no match, return null (as
     * opposed to the spring template that throws an exception)
     */
    protected <T> T queryForObject(String sql, RowMapper<T> rm, Object... args) {
        try {
            return jdbcTemplate.queryForObject(sql, rm, args);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Performs an insert to a table that has auto generated key, and return the
     * new key. Supports insert of ONE record at a time.
     */
    protected long insert(String sql, Object... params) {
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        int inserts = jdbcTemplate.update(new SimplePreparedStatementCreator(sql, params), key);
        if (inserts == 0)
            throw new EmptyResultDataAccessException("Insert did not create any rows\n" + sql, inserts);

        if (inserts != 1)
            throw new IncorrectResultSizeDataAccessException("Insert for a single row created " + inserts
                                                             + " rows\n" + sql, 1, inserts);

        return key.getKey().longValue();
    }

    /**
     * PreparedStatementCreator specific for insert statements.
     */
    private static class SimplePreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

        private final String sql;
        private final Object[] args;

        public SimplePreparedStatementCreator(String sql, Object... args) {
            Assert.notNull(sql, "SQL must not be null");
            this.sql = sql;
            this.args = args;
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            final PreparedStatement ps = con.prepareStatement(this.sql, Statement.RETURN_GENERATED_KEYS);
            setValues(ps);
            return ps;
        }

        public String getSql() {
            return this.sql;
        }

        public void setValues(PreparedStatement ps) throws SQLException {
            if (this.args != null) {
                for (int i = 0; i < this.args.length; i++) {
                    Object arg = this.args[i];
                    doSetValue(ps, i + 1, arg);
                }
            }
        }

        protected void doSetValue(PreparedStatement ps, int parameterPosition, Object argValue)
            throws SQLException {
            if (argValue instanceof SqlParameterValue) {
                SqlParameterValue paramValue = (SqlParameterValue)argValue;
                StatementCreatorUtils.setParameterValue(ps, parameterPosition, paramValue,
                                                        paramValue.getValue());
            } else {
                StatementCreatorUtils.setParameterValue(ps, parameterPosition, SqlTypeValue.TYPE_UNKNOWN,
                                                        argValue);
            }
        }
    }

    public static final RowMapper<String> STRING_ROW_MAPPER = new RowMapper<String>() {
        @Override
        public String mapRow(ResultSet rs, int i) throws SQLException {
            return rs.getString(1);
        }
    };

    protected static final RowMapper<Long> LONG_ROW_MAPPER = new RowMapper<Long>() {
        @Override
        public Long mapRow(ResultSet rs, int i) throws SQLException {
            return rs.getLong(1);
        }
    };
}
