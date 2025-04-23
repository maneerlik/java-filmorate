# Filmorate Database Schema

## Database Structure

---

### Tables

#### `users` - Stores user information
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(50) NOT NULL,
    name VARCHAR(100),
    birthday DATE,
    CONSTRAINT email_unique UNIQUE (email),
    CONSTRAINT login_unique UNIQUE (login)
);
```

#### `films` - Stores film information
```sql
CREATE TABLE films (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    release_date DATE,
    duration BIGINT NOT NULL,
    mpa_rating_id INTEGER REFERENCES mpa_ratings(id),
    CONSTRAINT duration_positive CHECK (duration > 0)
);
```

#### `mpa_ratings` - Movie rating categories
```sql
CREATE TABLE mpa_ratings (
    id SERIAL PRIMARY KEY,
    name VARCHAR(10) NOT NULL,
    description VARCHAR(100)
);
```

#### `genres` - Film genres
```sql
CREATE TABLE genres (
    id SERIAL PRIMARY KEY,
    name VARCHAR(55) NOT NULL
);
```
---

### Relationship Tables
#### `film_genres` - Many-to-many films to genres
```sql
CREATE TABLE film_genres (
    film_id BIGINT REFERENCES films(id) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);
```

#### `user_friends` - Many-to-many users to friends
```sql
CREATE TABLE user_friends (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    friend_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT no_self_friend CHECK (user_id <> friend_id)
);
```

#### `film_likes` - Many-to-many films to user likes
```sql
CREATE TABLE film_likes (
    film_id BIGINT REFERENCES films(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);
```
---

### Indexes
```sql
CREATE INDEX idx_films_mpa_rating ON films(mpa_rating_id);
CREATE INDEX idx_film_genres_film ON film_genres(film_id);
CREATE INDEX idx_film_genres_genre ON film_genres(genre_id);
CREATE INDEX idx_user_friends_user ON user_friends(user_id);
CREATE INDEX idx_user_friends_friend ON user_friends(friend_id);
CREATE INDEX idx_film_likes_film ON film_likes(film_id);
CREATE INDEX idx_film_likes_user ON film_likes(user_id);
```