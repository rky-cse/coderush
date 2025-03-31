package me.rkycse.coderush.service;

import me.rkycse.coderush.entity.UpdatedRating;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingUpdateStagingService {

    private final JdbcTemplate jdbcTemplate;

    public RatingUpdateStagingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void bulkUpdateUserRatings(List<UpdatedRating> updatedRatings) {
        // 1. Create a temporary staging table.
        String createTempTableSql = "CREATE TEMP TABLE staging_user_ratings (" +
                "user_name VARCHAR(255), " +
                "new_rating BIGINT" +
                ")";
        jdbcTemplate.execute(createTempTableSql);

        // 2. Insert updated ratings into the staging table in batches.
        String insertSql = "INSERT INTO staging_user_ratings (user_name, new_rating) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(insertSql, updatedRatings, 1000, (ps, updatedRating) -> {
            ps.setString(1, updatedRating.getUserName());
            ps.setLong(2, updatedRating.getNewRating());
        });

        // 3. Perform a bulk update using a JOIN between the staging table and the main users table.
        String updateSql = "UPDATE users u SET rating = s.new_rating " +
                "FROM staging_user_ratings s " +
                "WHERE u.username = s.user_name";
        int updatedRows = jdbcTemplate.update(updateSql);
        System.out.println("Bulk updated rows: " + updatedRows);

        // 4. Drop the staging table (optional; it will be dropped at session end).
        jdbcTemplate.execute("DROP TABLE staging_user_ratings");
    }
}
