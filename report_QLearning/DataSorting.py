# 学習回数が少ない順に並び変えるプログラム
import pandas as pd

def main():
    # ユーザーにCSVファイルのパスを入力させる
    print("=== Q学習データ分析 ===")
    file_path = input("CSVファイルのパスを入力してください（例: output.csv）:\n")

    try:
        # CSVファイルを読み込む
        df = pd.read_csv(file_path, header=None, names=['データ'])
        
        # データを分割して新しいカラムを作成
        df[['ε', 'α', 'γ', '学習回数', 'ステップ数']] = df['データ'].str.extract(r'ε:(\d+\.\d+) α:(\d+\.\d+) γ:(\d+\.\d+) 学習回数:(\d+) ステップ数:(\d+)')
        
        # データ型を変換
        df['ε'] = df['ε'].astype(float)
        df['α'] = df['α'].astype(float)
        df['γ'] = df['γ'].astype(float)
        df['学習回数'] = df['学習回数'].astype(int)
        df['ステップ数'] = df['ステップ数'].astype(int)

        # 学習回数で並べ替え
        sorted_df = df.sort_values(by='学習回数')

        # すべての行を表示できるように設定
        pd.set_option('display.max_rows', None)  # Noneに設定するとすべての行を表示
        pd.set_option('display.max_columns', None)  # すべての列を表示

        # 並べ替えた結果を表示
        print("\n=== 学習回数が少ない順に並べ替えた結果 ===")
        print(sorted_df[['ε', 'α', 'γ', '学習回数', 'ステップ数']])

        # 並べ替えた結果をCSVファイルに保存
        output_file_path = 'DataAnalysis.csv'
        sorted_df.to_csv(output_file_path, index=False, encoding='utf-8-sig')
        print(f"\n結果を {output_file_path} に保存しました。")

    except FileNotFoundError:
        print(f"ファイルが見つかりません: {file_path}")
    except Exception as e:
        print(f"エラーが発生しました: {e}")

if __name__ == "__main__":
    main()