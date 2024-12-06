-- 국가별 전체 클릭수를 저장하는 테이블
CREATE TABLE region_pops (
                             region_code VARCHAR(10) PRIMARY KEY,
                             count BIGINT NOT NULL DEFAULT 0,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 방문자별 클릭수를 저장하는 테이블
CREATE TABLE visitor_pops (
                              ip_address VARCHAR(45) NOT NULL,
                              region_code VARCHAR(10) NOT NULL,
                              count BIGINT NOT NULL DEFAULT 0,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                              PRIMARY KEY (ip_address, region_code)
);

-- 리더보드 조회를 위한 인덱스
CREATE INDEX idx_region_pops_count ON region_pops(count DESC);

-- region_code 조회를 위한 인덱스
CREATE INDEX idx_visitor_pops_region_code ON visitor_pops(region_code);
