-- ── Onboarding profiles ─────────────────────────────────────────────────────
CREATE TABLE onboarding_profiles
(
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT        NOT NULL UNIQUE REFERENCES users (id),
    health_conditions   TEXT,
    allergies           VARCHAR(500),
    dietary_preference  VARCHAR(30),
    goal                VARCHAR(200),
    age                 INT,
    gender              VARCHAR(10),
    height_cm           DECIMAL(6, 2),
    weight_kg           DECIMAL(6, 2),
    bmi_value           DECIMAL(5, 2),
    bmi_category        VARCHAR(50),
    complete            BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Diet plans ───────────────────────────────────────────────────────────────
CREATE TABLE diet_plans
(
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(200)   NOT NULL,
    description       TEXT,
    target_calories   INT,
    target_protein_g  DECIMAL(7, 2),
    target_carbs_g    DECIMAL(7, 2),
    target_fat_g      DECIMAL(7, 2),
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Client diet plan assignments ─────────────────────────────────────────────
CREATE TABLE client_diet_plan_assignments
(
    id           BIGSERIAL PRIMARY KEY,
    client_id    BIGINT NOT NULL REFERENCES users (id),
    diet_plan_id BIGINT NOT NULL REFERENCES diet_plans (id),
    assigned_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Client notes ─────────────────────────────────────────────────────────────
CREATE TABLE client_notes
(
    id         BIGSERIAL PRIMARY KEY,
    client_id  BIGINT NOT NULL REFERENCES users (id),
    note       TEXT   NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Meal feedback ────────────────────────────────────────────────────────────
CREATE TABLE meal_feedback
(
    id          BIGSERIAL PRIMARY KEY,
    client_id   BIGINT NOT NULL REFERENCES users (id),
    meal_log_id BIGINT,
    feedback    TEXT   NOT NULL,
    rating      INT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Transactions ─────────────────────────────────────────────────────────────
CREATE TABLE transactions
(
    id             BIGSERIAL PRIMARY KEY,
    client_id      BIGINT         NOT NULL REFERENCES users (id),
    type           VARCHAR(30)    NOT NULL,
    amount         DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50),
    description    VARCHAR(500),
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Appointments ─────────────────────────────────────────────────────────────
CREATE TABLE appointments
(
    id                    BIGSERIAL PRIMARY KEY,
    client_id             BIGINT      NOT NULL REFERENCES users (id),
    appointment_date      DATE        NOT NULL,
    appointment_time      VARCHAR(20) NOT NULL,
    type                  VARCHAR(20) NOT NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'pending',
    meet_link             VARCHAR(500),
    pre_consult_submitted BOOLEAN     NOT NULL DEFAULT FALSE,
    rating                INT,
    notes                 TEXT,
    created_at            TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at            TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Pre-consult forms ────────────────────────────────────────────────────────
CREATE TABLE pre_consult_forms
(
    id                   BIGSERIAL PRIMARY KEY,
    appointment_id       BIGINT NOT NULL UNIQUE REFERENCES appointments (id),
    current_concerns     TEXT,
    recent_diet_changes  TEXT,
    current_medications  TEXT,
    lab_report_urls      TEXT,
    additional_notes     TEXT,
    submitted_at         TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at           TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at           TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Session summaries ────────────────────────────────────────────────────────
CREATE TABLE session_summaries
(
    id                 BIGSERIAL PRIMARY KEY,
    appointment_id     BIGINT NOT NULL UNIQUE REFERENCES appointments (id),
    notes              TEXT   NOT NULL,
    recommendations    TEXT,
    follow_up_required BOOLEAN DEFAULT FALSE,
    client_rating      INT,
    client_feedback    TEXT,
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at         TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Meal logs ────────────────────────────────────────────────────────────────
CREATE TABLE meal_logs
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT         NOT NULL REFERENCES users (id),
    date       DATE           NOT NULL,
    meal_type  VARCHAR(20)    NOT NULL,
    meal_name  VARCHAR(200)   NOT NULL,
    kcal       DECIMAL(8, 2)  NOT NULL,
    protein    DECIMAL(7, 2)  DEFAULT 0,
    carbs      DECIMAL(7, 2)  DEFAULT 0,
    fat        DECIMAL(7, 2)  DEFAULT 0,
    fiber      DECIMAL(7, 2)  DEFAULT 0,
    source     VARCHAR(20)    DEFAULT 'custom',
    photo_url  VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Meal plans ───────────────────────────────────────────────────────────────
CREATE TABLE meal_plans
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Meal plan items ──────────────────────────────────────────────────────────
CREATE TABLE meal_plan_items
(
    id             BIGSERIAL PRIMARY KEY,
    meal_plan_id   BIGINT       NOT NULL REFERENCES meal_plans (id) ON DELETE CASCADE,
    meal_type      VARCHAR(20)  NOT NULL,
    name           VARCHAR(200) NOT NULL,
    kcal           DECIMAL(8, 2),
    protein        DECIMAL(7, 2),
    carbs          DECIMAL(7, 2),
    fat            DECIMAL(7, 2),
    fiber          DECIMAL(7, 2),
    ingredients    TEXT,
    prep_notes     TEXT,
    scheduled_time VARCHAR(20),
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Client meal plan assignments ─────────────────────────────────────────────
CREATE TABLE client_meal_plan_assignments
(
    id           BIGSERIAL PRIMARY KEY,
    client_id    BIGINT NOT NULL REFERENCES users (id),
    meal_plan_id BIGINT NOT NULL REFERENCES meal_plans (id),
    assigned_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Food items ───────────────────────────────────────────────────────────────
CREATE TABLE food_items
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    serving_size_g   DECIMAL(8, 2),
    kcal_per_100g    DECIMAL(8, 2),
    protein_per_100g DECIMAL(7, 2),
    carbs_per_100g   DECIMAL(7, 2),
    fat_per_100g     DECIMAL(7, 2),
    fiber_per_100g   DECIMAL(7, 2),
    source           VARCHAR(50),
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Weight entries ───────────────────────────────────────────────────────────
CREATE TABLE weight_entries
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT        NOT NULL REFERENCES users (id),
    weight_kg  DECIMAL(6, 2) NOT NULL,
    date       DATE          NOT NULL,
    notes      TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Weight goals ─────────────────────────────────────────────────────────────
CREATE TABLE weight_goals
(
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT        NOT NULL UNIQUE REFERENCES users (id),
    goal_weight_kg DECIMAL(6, 2) NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Measurement entries ──────────────────────────────────────────────────────
CREATE TABLE measurement_entries
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users (id),
    date       DATE   NOT NULL,
    waist_cm   DECIMAL(6, 2),
    hip_cm     DECIMAL(6, 2),
    chest_cm   DECIMAL(6, 2),
    arm_cm     DECIMAL(6, 2),
    thigh_cm   DECIMAL(6, 2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Step entries ─────────────────────────────────────────────────────────────
CREATE TABLE step_entries
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users (id),
    date       DATE   NOT NULL,
    steps      INT    NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uq_step_user_date UNIQUE (user_id, date)
);

-- ── Hydration entries ────────────────────────────────────────────────────────
CREATE TABLE hydration_entries
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users (id),
    date       DATE   NOT NULL,
    ml         INT    NOT NULL,
    source     VARCHAR(100),
    logged_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Hydration goals ──────────────────────────────────────────────────────────
CREATE TABLE hydration_goals
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL UNIQUE REFERENCES users (id),
    goal_ml    INT    NOT NULL DEFAULT 2000,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Products ─────────────────────────────────────────────────────────────────
CREATE TABLE products
(
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(200)   NOT NULL,
    description    TEXT,
    price          DECIMAL(10, 2) NOT NULL,
    category       VARCHAR(100),
    image_url      VARCHAR(500),
    stock_level    INT            NOT NULL DEFAULT 0,
    active         BOOLEAN        NOT NULL DEFAULT TRUE,
    nutrition_info TEXT,
    ingredients    TEXT,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Product reviews ──────────────────────────────────────────────────────────
CREATE TABLE product_reviews
(
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products (id),
    user_id    BIGINT NOT NULL REFERENCES users (id),
    rating     INT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uq_product_user_review UNIQUE (product_id, user_id)
);

-- ── Orders ───────────────────────────────────────────────────────────────────
CREATE TABLE orders
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT         NOT NULL REFERENCES users (id),
    subtotal         DECIMAL(10, 2) NOT NULL,
    handling_fee     DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total            DECIMAL(10, 2) NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'pending',
    payment_method   VARCHAR(20)    NOT NULL,
    delivery_label   VARCHAR(100),
    delivery_line1   VARCHAR(500)   NOT NULL,
    delivery_line2   VARCHAR(500),
    delivery_phone   VARCHAR(20)    NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Order items ──────────────────────────────────────────────────────────────
CREATE TABLE order_items
(
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id   BIGINT         NOT NULL REFERENCES products (id),
    product_name VARCHAR(200)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(10, 2) NOT NULL,
    subtotal     DECIMAL(10, 2) NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Wishlists ────────────────────────────────────────────────────────────────
CREATE TABLE wishlists
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users (id),
    product_id BIGINT NOT NULL REFERENCES products (id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uq_wishlist_user_product UNIQUE (user_id, product_id)
);

-- ── Indexes ──────────────────────────────────────────────────────────────────
CREATE INDEX idx_onboarding_user_id       ON onboarding_profiles (user_id);
CREATE INDEX idx_diet_plan_assignments    ON client_diet_plan_assignments (client_id);
CREATE INDEX idx_client_notes_client      ON client_notes (client_id);
CREATE INDEX idx_meal_feedback_client     ON meal_feedback (client_id);
CREATE INDEX idx_transactions_client      ON transactions (client_id);
CREATE INDEX idx_appointments_client      ON appointments (client_id);
CREATE INDEX idx_appointments_status      ON appointments (status);
CREATE INDEX idx_meal_logs_user_date      ON meal_logs (user_id, date);
CREATE INDEX idx_meal_plan_items_plan     ON meal_plan_items (meal_plan_id);
CREATE INDEX idx_client_meal_assignments  ON client_meal_plan_assignments (client_id);
CREATE INDEX idx_weight_entries_user_date ON weight_entries (user_id, date);
CREATE INDEX idx_measurement_user_date    ON measurement_entries (user_id, date);
CREATE INDEX idx_step_entries_user_date   ON step_entries (user_id, date);
CREATE INDEX idx_hydration_user_date      ON hydration_entries (user_id, date);
CREATE INDEX idx_product_reviews_product  ON product_reviews (product_id);
CREATE INDEX idx_order_items_order        ON order_items (order_id);
CREATE INDEX idx_orders_user              ON orders (user_id);
CREATE INDEX idx_wishlist_user            ON wishlists (user_id);
