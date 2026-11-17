"""
Create 500 fake users in the Neo4j database.

This matches the real User model in:
backend/user-service/src/main/java/com/neo/user_service/models/User.java

It creates 2 fixed admin accounts (exact info given by the project owner),
498 random regular users, and some random friendships between them.

How to run:
    pip install neo4j bcrypt faker
    export NEO4J_PASSWORD=<same password as in your .env file>
    python scripts/generate_users.py
"""

import os
import random
import uuid
from datetime import datetime, timezone

import bcrypt
from faker import Faker
from neo4j import GraphDatabase

NEO4J_URI = "bolt://localhost:7687"
NEO4J_USER = "neo4j"

NEO4J_PASSWORD = os.environ.get("NEO4J_PASSWORD")
HAMZA_PASSWORD = os.environ.get("HAMZA_PASSWORD")
ADNANE_PASSWORD = os.environ.get("ADNANE_PASSWORD")
USERS_PASSWORD = os.environ.get("USERS_PASSWORD")

TOTAL_USERS = 500
FRIENDS_PER_USER = 5


fake = Faker()


def hash_password(plain_password):
    return bcrypt.hashpw(plain_password.encode(), bcrypt.gensalt()).decode()


def build_admin(first_name, last_name, username, email, plain_password):
    now = datetime.now(timezone.utc)
    return {
        "id": str(uuid.uuid4()),
        "firstName": first_name,
        "lastName": last_name,
        "username": username,
        "email": email,
        "password": hash_password(plain_password),
        "role": "ADMIN",
        "twoFactorEnabled": False,
        "createdAt": now,
        "updatedAt": now,
    }


def build_random_user(index, hashed_default_password):
    now = datetime.now(timezone.utc)
    first_name = fake.first_name()
    last_name = fake.last_name()
    # index makes the username/email unique, even if two fake names repeat
    username = f"{first_name.lower()}{last_name.lower()}{index}"
    email = f"{username}@example.com"

    return {
        "id": str(uuid.uuid4()),
        "firstName": first_name,
        "lastName": last_name,
        "username": username,
        "email": email,
        "password": hashed_default_password,
        "role": "USER",
        "twoFactorEnabled": False,
        "createdAt": now,
        "updatedAt": now,
    }


def build_users():
    users = [
        build_admin("Hamza", "Elkhawlani", "heeemzaaa", "hamza@gmail.com", HAMZA_PASSWORD),
        build_admin("Adnane", "Elmir", "von", "adnane@gmail.com", ADNANE_PASSWORD),
    ]

    hashed_default_password = hash_password(USERS_PASSWORD)
    for i in range(TOTAL_USERS - len(users)):
        users.append(build_random_user(i, hashed_default_password))

    return users


def build_friendships(users):
    ids = [u["id"] for u in users]
    pairs = []

    for user_id in ids:
        others = [uid for uid in ids if uid != user_id]
        chosen = random.sample(others, min(FRIENDS_PER_USER, len(others)))
        for friend_id in chosen:
            # friendship goes both ways
            pairs.append({"from": user_id, "to": friend_id})
            pairs.append({"from": friend_id, "to": user_id})

    return pairs


def main():
    if not NEO4J_PASSWORD:
        raise SystemExit("Set the NEO4J_PASSWORD environment variable first (same value as in .env).")

    users = build_users()
    friendships = build_friendships(users)

    driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASSWORD))

    with driver.session() as session:
        session.run(
            """
            UNWIND $users AS u
            CREATE (n:User {
                id: u.id,
                firstName: u.firstName,
                lastName: u.lastName,
                username: u.username,
                email: u.email,
                password: u.password,
                role: u.role,
                twoFactorEnabled: u.twoFactorEnabled,
                createdAt: u.createdAt,
                updatedAt: u.updatedAt
            })
            """,
            users=users,
        )

        session.run(
            """
            UNWIND $pairs AS p
            MATCH (a:User {id: p.from}), (b:User {id: p.to})
            MERGE (a)-[:FRIEND_OF]->(b)
            """,
            pairs=friendships,
        )

    driver.close()

    print(f"Created {len(users)} users and {len(friendships)} friendships.")
    print(f"Admin 1 -> username: heeemzaaa, password: {HAMZA_PASSWORD}")
    print(f"Admin 2 -> username: von, password: {ADNANE_PASSWORD}")
    print(f"All other users share the password: {USERS_PASSWORD}")


if __name__ == "__main__":
    main()
