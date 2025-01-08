#!/bin/bash

# デフォルトのJARファイル
DEFAULT_JAR_FILE="FreTreOperation.jar"
# デフォルトの実行コマンド種別
DEFAULT_RUN_JOB_TYPE="clean"

# 引数からJARファイル名を取得（指定がない場合はデフォルト値を使用）
JAR_FILE=${1:-$DEFAULT_JAR_FILE}

# 実行モード（テスト or staging or 本番）
MODE=${2:-"staging"}

# 実行コマンド種別
RUN_JOB_TYPE=${3:-$DEFAULT_RUN_JOB_TYPE}

# 実行モードに応じた設定
if [ "$MODE" == "develop" ]; then
  echo "テスト実行モード: 環境変数を設定します。"
  export FIRESTORE_EMULATOR_HOST="127.0.0.1:8080"
elif [ "$MODE" == "staging" ]; then
  echo "STAGING実行モード: 環境変数を設定します。"
  unset FIRESTORE_EMULATOR_HOST
elif [ "$MODE" == "production" ]; then
  echo "本番実行モード: 環境変数をクリアします。"
  unset FIRESTORE_EMULATOR_HOST
else
  echo "エラー: 実行モードは 'develop' or 'staging' or 'production' を指定してください。"
  exit 1
fi

# 実行コマンド種別の確認
if [ "$RUN_JOB_TYPE" == "clean" ] || [ "$RUN_JOB_TYPE" == "testInsert" ] || [ "$RUN_JOB_TYPE" == "testDelete" ]; then
  echo "$RUN_JOB_TYPE を実行します!"
else
  echo "エラー: 実行コマンド種別は 'clean' or 'testInsert' or 'testDelete' を指定してください。"
  exit 1
fi

# JARファイルの存在を確認
if [ ! -f "$JAR_FILE" ]; then
  echo "エラー: 指定されたJARファイル '$JAR_FILE' が見つかりません。"
  exit 1
fi

# JARファイルを実行
echo "JARファイル '$JAR_FILE' を実行します。モード: $MODE, コマンド: $RUN_JOB_TYPE"
java -jar "$JAR_FILE" $MODE $RUN_JOB_TYPE
