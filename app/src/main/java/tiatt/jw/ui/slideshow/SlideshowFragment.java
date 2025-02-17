/*
https://m.blog.naver.com/jain5480/221195553416 Fragment 오류시 참고
 */

package tiatt.jw.ui.slideshow;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import tiatt.jw.MainActivity;
import tiatt.jw.R;
import tiatt.jw.ui.tools.ToolsFragment;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;


public class SlideshowFragment extends Fragment implements TextToSpeech.OnInitListener {

    TextToSpeech tts;

    Button btnTranslate;
    Button btnTTSRead;
    Button btnTTSRead2;
    Button btnTTSWrite;
    EditText textConvertInput;
    TextView textConvertResult;


    private SlideshowViewModel slideshowViewModel;

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1003)
        {

            if (data == null)
            {
                Toast.makeText(getActivity(), "입력받은 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
            } else
                {
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                textConvertInput.setText(results.get(0));
            }

        }
    }
    Context context;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                ViewModelProviders.of(this).get(SlideshowViewModel.class);


        View view = getLayoutInflater().inflate(R.layout.fragment_slideshow, container, false);
        context = container.getContext();





        return view;




    }






    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);




        btnTranslate = getView().findViewById(R.id.btnTranslate);
        textConvertResult = getView().findViewById(R.id.textConvertResult);


        textConvertInput = getView().findViewById(R.id.textConvertInput);
        btnTTSRead = getView().findViewById(R.id.btnTTSRead);
        btnTTSRead2 = getView().findViewById(R.id.btnTTSRead2);
        btnTTSWrite = getView().findViewById(R.id.btnTTSWrite);


        tts = new TextToSpeech(getActivity(), this);
        textConvertResult.setEnabled(true);

        btnTTSRead.setOnClickListener(view -> speakOutNow());
        btnTTSRead2.setOnClickListener(view -> speakOutNow2());
        btnTranslate.setOnClickListener(view -> Translate());

        textConvertInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void afterTextChanged(Editable editable) {
                Translate();
                System.out.println(textConvertInput.getText().toString());
                if(textConvertInput.getText().toString().equals(""))
                {
                    textConvertResult.setText("바꾼 글자나 문장은 여기에 뜹니다.");
                }
            }
        });

        btnTTSWrite.setOnClickListener(v ->
                startActivityForResult(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech Recognition"), 1003));


        // 클립보드
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        String pasteData = "";

// 클립보드에 데이터가 없거나 텍스트 타입이 아닌 경우
        if (!(clipboard.hasPrimaryClip())) {
            ;
        }
        else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
            ;
        }
        else {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            pasteData = item.getText().toString();
            textConvertInput.setText(pasteData);

        }

        // 알림
        //ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                Log.i("clipboard", "changed to:" + clipboard.getText());
            }
        });
/*
        final Timer timer;
        TimerTask timerTask;
        final long time = 1000;
        final long lastTime = System.currentTimeMillis();

        timer = new Timer(true);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                Translate();
            }
        };
        timer.schedule(timerTask, 0, 10000);

*/
    }


    String[] ChangeArr[] = {

             //참고 : https://projectresearch.co.kr/2012/10/03/%EB%A9%8B%EC%A7%84-%EC%9A%B0%EB%A6%AC%EB%A7%90-%EC%88%9C%ED%99%94-%EB%AA%A9%EB%A1%9D-%EA%B5%AD%EB%A6%BD%EA%B5%AD%EC%96%B4%EC%9B%90-%EC%9A%B0%EB%A6%AC%EB%A7%90-%EB%8B%A4%EB%93%AC%EA%B8%B0/
            //참고2 : https://endic.naver.com/
            // 1단계
            // 바꾸기 전 단어 / 바꿀 단어 / 어원 / 뜻 / 다른 단어
            {"가십", "입방아", "gossip", "소문이 될 만한 내용", ""},
            {"갈라쇼", "뒤풀이공연", "gala show", "큰 경기나 공연이 끝나고 나서, 축하하여 벌이는 큰 규모의 오락 행사.", ""},
            {"게이트", "문", "gate", "정치가·정부 관리와 관련된, 비리 의혹에 싸여 있는 사건.", "개표구, 의혹사건"},
            {"골드미스", "황금독신여성", "Gold Miss", "30대 이상 40대 미만 미혼 여성 중 학력이 높고 사회적, 경제적 여유를 갖추고 있는 계층.", ""},
            {"교례회", "어울모임", "交禮會", "어떤 단체, 조직의 구성원들이 특정한 날, 일을 계기로 어울리며 덕담을 주고받는 모임이나 행사.", ""},
            {"그라피티", "길거리 그림", "graffiti", "길거리 여기저기 벽면에 낙서처럼 그리거나 페인트를 분무기로 내뿜어서 그리는 그림.", ""},
            {"그래피티", "길거리 그림", "graffiti", "길거리 여기저기 벽면에 낙서처럼 그리거나 페인트를 분무기로 내뿜어서 그리는 그림.", ""},
            {"그룹", "모임" , "group", "(한 곳에 모인·서로 관련이 있는) 무리, 모임", ""},
            {"프리미엄", "고급", "premium", "", "차익"},
            {"럭셔리", "고급", "luxury", "", ""},
            {"글램핑", "야영", "glamping", "비용이 많이 들어가는 고급스러운 야영을 뜻함.", ""},
            {"캠핑", "야영", "camping", "", ""},
            {"네비게이션", "길도우미", "navigation", "지도를 보이거나 지름길을 찾아 주어 자동차 운전을 도와주는 장치나 프로그램.", ""},
            {"내비게이션", "길도우미", "navigation", "지도를 보이거나 지름길을 찾아 주어 자동차 운전을 도와주는 장치나 프로그램.", ""},
            {"네티즌", "누리꾼", "netizen", "정보 통신망이 제공하는 새로운 세계에서 마치 그 세계의 시민처럼 활동하는 사람.", ""},
            {"네이미스트", "이름 설계사", "namist", "기업명, 상표명, 도메인명, 인명 등의 이름을 전문적으로 짓는 사람.", ""},
            {"노미네이트", "후보 지명", "nominate", "흔히 ‘노미네이트되다’라고 표현하는데, 이는 시상 행사와 관련해 ‘어떤 상의 후보자로 지명되다.’라는 뜻을 지님.", ""},
            {"노블리스", "귀족", "noblesse", "", "고귀한 사람, 지도층"},
            {"노블레스", "귀족", "noblesse", "", "고귀한 사람, 지도층"},
            {"오블리주", "의무", "Oblige", "", ""},
            {"마케팅", "홍보", "marketing", "", ""},
            {"노이즈", "잡음", "noise", "", ""},
            {"뉴타운", "새누리촌", "new town", "도시 개발 정책의 일환으로 지방 자치 단체나 정부가 지정하여 재개발하는 도시 속의 도시를 뜻함.", ""},
            {"다이", "손수 만들기", "DIY", "부품이나 재료를 구입해서 소비자가 직접 조립하여 제품을 만드는 일.", "죽음"},
            {"DIY", "손수 만들기", "DIY", "부품이나 재료를 구입해서 소비자가 직접 조립하여 제품을 만드는 일.", "죽음"},
            {"다크서클", "눈그늘", "dark circle", "눈 아랫부분이 거무스름하게 그늘이 지는 것을 가리킴.", ""},
            {"드레스코드", "표준 옷차림", "dress code", "어떤 모임의 목적, 시간, 만나는 사람 등등에 따라 갖추어야 할 옷차림새를 가리킴."},
            {"다크 투어리즘", "역사 교훈 여행", "dark tourism", "재난 현장이나 참상지 등 역사적인 비극의 현장을 방문하는 여행.", ""},
            {"데카르트 마케팅", "예술 감각 상품", "techart marketing", "기술tech과 예술art을 합친 말.", ""},
            {"드라이브", "운전", "drive", "", "몰아가기"},
            {"더치페이", "각자 내기", "Dutch pay", "비용을 각자 부담하는 것.", ""},
            {"드레싱", "맛깔장", "dressing", "야채, 육류, 생선 따위의 식품에 치는 소스를 가리킴.", ""},
            {"드로어즈", "맵시 속바지", "drawers", "남성의 삼각팬티와 사각팬티를 절충하여 만든 팬티로서 그 끝이 가랑이까지 늘어지는 것을 가리킴.", ""},
            {"드로즈", "맵시 속바지", "drawers", "남성의 삼각팬티와 사각팬티를 절충하여 만든 팬티로서 그 끝이 가랑이까지 늘어지는 것을 가리킴.", ""},
            {"디엠", "쪽지", "DM - Direct Message", "", "광고물"},
            {"DM", "쪽지", "DM - Direct Message", "", "광고물"},
            {"디오라마", "실사 모형", "diorama", "여러 가지 소품들로 적절한 배경과 함께 하나의 상황이나 장면을 구성해 내는 것.", ""},
            {"딩펫족", "맞벌이 애완족", "Dinkpet族", "정상적인 부부 생활을 하면서도 의도적으로 자녀를 두지 않고, 애완동물을 기르며 사는 맞벌이 부부.", ""},
            {"랜드마크", "상징물", "landmark", "산마루처럼 우뚝한 지형지물이나 도시 경관.", ""}, //마루지
            {"러브", "사랑", "love", "", ""},
            {"라인", "선", "line", "", "구도"},
            {"라이프", "생활", "life", "", ""},
            {"콜", "전화", "call", "", ""},
            {"레시피", "조리법", "recipe", "음식의 조리법을 뜻하는 요리 용어를 가리켜 이르는 말.", ""},
            {"레이싱걸", "행사빛냄이", "racing girl", "‘자동차 경주가 있을 때 관중 동원이나 홍보 효과를 높이기 위해서 동원되는 젊은 여성’을 가리켜 이르는 말.", ""},
            {"레퍼런스", "참조", "reference", "디브이디(DVD)나 시디(CD) 가운데 뛰어난 음질과 화질을 갖춘 최고의 것을 가리킴.", "언급, 고품질"},
            {"로고송", "상징 노래", "logo song", "특정 상품, 회사, 개인의 상징적 이미지를 심어 주고 널리 알리기 위하여 사용하는 노래.", ""},
            {"로드무비", "여정 영화", "road movie", "주인공이 여행을 통하여 인간관계의 새로운 계기를 마련하거나 자신의 정체성을 바로 세우게 되는 과정을 그린 영화.", ""},
            {"로드킬", "찻길 동물 사고", "roadkill", "찻길에서 동물이 당하는 교통사고를 가리킴.", ""},
            {"로밍", "어울통신", "roaming", "통신 회사끼리 제휴를 맺어 서로의 통신망에 접속할 수 있도록 하여 어느 곳에서든 품질 좋은 서비스를 제공하는 일. ‘어울’은 ‘어울다’의 어간, ‘어울다’는 ‘어우르다’의 옛말.", ""},
            {"로하스", "친환경살이", "LOHAS - Lifestyle Of Health And Sustainability", "건강과 환경의 지속 가능성을 생각하고 실천하는 생활 방식’을 이르는 말.", ""},
            {"론칭쇼", "신제품 발표회", "launching show", "어떤 제품이나 상표의 공식적인 출시를 알리는 행사를 이르는 말.", ""},
            {"런칭쇼", "신제품 발표회", "launching show", "어떤 제품이나 상표의 공식적인 출시를 알리는 행사를 이르는 말.", ""},
            {"론칭", "개시", "launching", "어떤 제품이나 상표의 공식적인 출시", ""},
            {"런칭", "개시", "launching", "어떤 제품이나 상표의 공식적인 출시", ""},
            {"롤모델", "본보기상", "role model", "존경하며 본받고 싶도록 모범이 될 만한 사람 또는 자기의 직업, 업무, 임무, 역할 따위의 본보기가 되는 대상을 이르는 말.", ""},
            {"롤 모델", "본보기상", "role model", "존경하며 본받고 싶도록 모범이 될 만한 사람 또는 자기의 직업, 업무, 임무, 역할 따위의 본보기가 되는 대상을 이르는 말.", ""},
            {"리메이크", "재구성", "remake", "‘예전에 있던 영화, 음악, 드라마 따위를 새롭게 다시 만드는 것’을 이르는 말.", ""},
            {"리콜", "결함 보상", "recall", "회사 측이 제품의 결함을 발견하여 보상해 주는 소비자 보호 행위나 제도를 통틀어 이르는 말.", ""},
            {"리퍼브", "손질 상품", "refurbished", "불량 제품, 매장에서 전시되었던 제품, 소비자의 변심으로 반품된 제품 등을 다시 손질하여 소비자에게 정품보다 싸게 파는 것을 가리킴.", ""},
            {"리퍼", "손질 상품", "refurbished", "불량 제품, 매장에서 전시되었던 제품, 소비자의 변심으로 반품된 제품 등을 다시 손질하여 소비자에게 정품보다 싸게 파는 것을 가리킴.", ""},
            {"리플", "댓글", "Reply", "인터넷의 통신 공간에서 게시판에 올라 있는 글에 대해 덧붙이거나, 대답하거나, 비판하는 등의 짤막한 글을 가리켜 이르는 말.", ""},
            {"립싱크", "입술 연기", "lip sync", "텔레비전이나 영화에서, 화면에 나오는 배우나 가수의 입술 움직임과 음성을 일치시키는 일.", ""},
            {"마리나", "해안 유원지", "marina", "해변의 종합 관광 시설’을 뜻하는 말.", ""},
            {"마블링", "결지방", "marbling", "선홍색 살코기 사이에 하얀색 지방[우지(牛脂)]이 그물처럼 퍼져서 박혀 있는 것.", ""},
            {"마스터", "주인", "master", "", "명인"},
            {"마일리지", "이용실적", "mileage", "", "점수"},
            {"매치업", "맞대결", "match-up", "주로 농구에서 ‘둘 이상의 사람이나 물건이 짝을 이루거나 짝이 이루어지게 하는 일’이나 ‘한 선수가 상대 팀의 다른 한 선수와 맞대결하는 일’", ""},
            {"매치 업", "맞대결", "match-up", "주로 농구에서 ‘둘 이상의 사람이나 물건이 짝을 이루거나 짝이 이루어지게 하는 일’이나 ‘한 선수가 상대 팀의 다른 한 선수와 맞대결하는 일’", ""},
            {"멀티탭", "모둠꽂이", "multi-tap", "여러 개의 플러그를 꽂을 수 있게 만든 이동식 콘센트.", ""},
            {"머스트 해브", "필수품", "must have", "‘머스트 해브’는 필수로 가져야 할 물건이나 제품을 가리키는 외래어 ‘머스트 해브 아이템’의 줄인 말. 반드시 필요한 물건을 의미함.", ""},
            {"메세나", "문예후원", "mécénat", "특별한 대가를 바라지 않고 문화 예술 활동을 지원하는 기업이나 개인, 또는 그러한 활동을 이르는 말로 기본적으로 문화 예술 활동을 뒤에서 도와주는 일을 가리킴. ‘문예’는 ‘문화 예술’의 준말임. 로마의 정치가였던 ‘마에케나스’의 프랑스어 발음이 ‘메세나(mécénat)’임. 마에케나스는 당대의 시인들을 후원하면서 문화 예술의 보호자를 자처했다고 함.", ""},
            {"뷰파인더", "보기창", "viewfinder", "카메라에서, 눈을 대고 피사체를 보는 부분, 촬영할 사진의 구도나 초점 상태를 미리 볼 수 있도록 한 창을 가리켜 이르는 말.", "" },
            {"브랜드파워", "상표경쟁력", "brand power", "기업체의 상표가 가지는 힘을 뜻함.", ""},
            {"브로마이드", "벽붙이사진", "bromide", "‘고감도의 확대용 인화지’ 또는 ‘그 인화지에 현상한, 색이 변하지 않는 사진’ 또는 ‘배우·가수·운동선수 등의 초상 사진’을 가리켜 이르는 말.", ""},
            {"VOD","다시보기", "vod", "컴퓨터를 통해 원하는 프로그램이나 동영상물을 언제든지 다시 받아볼 수 있도록 해 주는 일을 가리켜 이르는 말.", ""},
            {"블라인드", "가림", "blind", "‘블라인드(blind)’는 ‘블라인드 면접’, ‘블라인드 인터뷰’, ‘블라인드 테스트’, ‘블라인드 시사회’ 등에서처럼 ‘어떤 내용이나 정보를 알 수 없음’의 의미로 쓰고 있음.", "가림막"},
            {"블랙 컨슈머", "악덕 소비자", "black consumer", "‘구매한 상품을 문제 삼아 피해를 본 것처럼 꾸며 악의적 민원을 제기하거나 보상을 요구하는 소비자’를 이르는 말.", ""},
            {"블로그", "누리사랑방", "blog", "개인이 자신의 관심사에 따라 자유롭게 글을 올릴 수 있는 웹사이트를 뜻함.", ""},
            {"블루오션", "대안시장", "blue ocean", "경쟁이 치열한 기존의 시장을 대체하는 시장을 가리키는 말로, ‘새로 개척하여 이윤을 많이 남길 수 있는, 경쟁이 거의 없는 시장’을 가리킴.",""},
            {"블루투스", "쌈지무선망", "bluetooth", "일정한 구역 안에서 무선으로 연결된 각종 정보 기기로 간편하게 정보를 주고받을 수 있는 통신망(또는 통신 기술)을 가리키는 말.", ""},
            {"비트박스", "입소리손장단", "beatbox", "입으로 소리를 내고 손으로 장단을 맞추어 강한 악센트의 리듬을 만들어 내는 일.", ""},
            {"빅리그", "최상위연맹", "big league", "프로 축구나 프로 야구 따위에서 가장 높은 위치나 등급에 속하는 리그를 가리키는 말.", ""},
            {"빙고", "맞았어", "bingo", "정답을 맞혔거나, 기대하지 않았던 결과에 대하여 기쁨을 표현하는 말, 주로 정답을 바로 맞혔음을 나타내는 말로 쓰임.", ""},
            {"사이버", "두루누리", "cyber", "인터넷 가상 공간", ""},
            {"샘플러", "맛보기묶음", "sampler", "음식과 관련해서 특정한 기준으로 선정한 일종의 표본, 음악과 관련해서 여러 음반에서 한 곡씩 선별하여 만든 작품집.\n" + "미리 경험할 수 있도록 대표적인 것 몇몇을 따로 골라서 모아 놓은 것.", ""},
            {"샹그릴라", "꿈의 낙원", "Shangri-la", "신비롭고 아름다운 이상향을 비유적으로 가리켜 이르는 말로, 인간이 바라는 희망이나 이상이 모두 실현되어 있어 편안하고 즐겁게 살 수 있는 곳을 가리켜 이르는 말.", ""},
            {"선루프", "지붕창", "sunroof", "바깥의 빛이나 공기가 차 안으로 들어오도록 조절할 수 있는 승용차(乘用車)의 지붕.", ""},
            {"선팅", "빛가림", "sunting", "창문, 자동차 등의 창유리로 들어오는 햇빛을 막기 위해 유리에 덧댄 검은색의 얇은 필름 또는 그런 필름을 덧대는 일을 가리킴.", ""},
            {"성큰 가든", "뜨락정원", "sunken garden", "빌딩이나 아파트 단지 안에 지하나 지하로 통하는 공간에 꾸민 정원.", ""},
            {"세트 피스", "맞춤전술", "set piece", "세트 피스는 축구에서 프리킥, 코너킥, 스로인 이후에 일어나는 조직적인 플레이를 가리키는 말로 ‘특정한 상황에 맞추어서 미리 계획해 놓은 대로 공격하는 전술’을 뜻함.", ""},
            {"셀슈머", "누리장터꾼", "sellsumer", "판매자(seller)와 소비자(consumer)가 합쳐진 말로, 인터넷상에서 물건을 사고파는 사람", ""},
            {"셀프카메라", "자가촬영", "self-camera", "자기 자신을 직접 사진이나 동영상으로 찍는 일.", ""},
            {"셀카", "자가촬영", "self-camera", "자기 자신을 직접 사진이나 동영상으로 찍는 일.", ""},
            {"SNS", "누리소통망", "SNS, Social Network Service", "온라인에서 인적 관계망 형성과 소통을 도와주는 서비스.", "누리소통망 서비스"},
            {"sns", "누리소통망", "SNS, Social Network Service", "온라인에서 인적 관계망 형성과 소통을 도와주는 서비스.", "누리소통망 서비스"},
            {"소셜 커머스", "공동 할인 구매", "social commerce", "누리소통망서비스(소셜 네트워크 서비스, SNS)을 이용한 전자 상거래의 일종.", ""},
            {"소셜커머스", "공동 할인 구매", "social commerce", "누리소통망서비스(소셜 네트워크 서비스, SNS)을 이용한 전자 상거래의 일종.", ""},
            {"소호", "무점포사업", "SOHO, Small Office Home Office", "특별한 사무실 없이 자신의 집을 사무실로 쓰는 소규모 자영업.", ""},
            {"소울메이트", "교감 지기", "soul mate", "똑같은 영혼을 가진 것처럼 생각이나 마음이 서로 잘 통하여 상대방의 속마음을 참되게 알아주는 친구.", ""},
            {"소울 메이트", "교감 지기", "soul mate", "똑같은 영혼을 가진 것처럼 생각이나 마음이 서로 잘 통하여 상대방의 속마음을 참되게 알아주는 친구.", ""},
            {"소울", "마음", "soul", "(한 사람의) 마음, (인간의) 정신", "정신"},
            {"쇼케이스", "선보임공연", "showcase", "새 음반이나 신인 가수를 관계자에게 널리 알리기 위해 갖는 특별 공연.", ""},
            {"쇼플러", "원정구매족","Shoppler",  "물건 구매를 뜻하는 쇼핑(Shopping)과 여행자를 뜻하는 트래블러(traveler)를 합성한 말로, 자신이 마음에 드는 물건을 사기 위해서 전 세계로 여행을 떠나는 소비자를 가리켜 이르는 말.", ""},
            {"숍인숍", "어울가게", "shop in shop", "매장 안에 또 다른 매장을 만들어 상품을 판매하는 새로운 형태의 매장.", ""},
            {"슈터링", "골문어림차기", "shootering", "슛인지 센터링인지 애매하게 골문 쪽을 향하여 공을 차는 일.", ""},
            {"스도쿠",  "숫자넣기",  "すどく, 數獨","가로세로가 아홉 칸씩으로 이루어진 정사각형의 가로줄과 세로줄에 1부터 9까지의 숫자를 겹치지 않도록 한 번씩 써서 채워 넣는 퍼즐(‘짜맞추기’ 또는 ‘알아맞히기’) 게임을 가리켜 이르는 말로, 여러 칸에 적절한 숫자를 넣는 놀이를 가리킴.", ""},
            {"스마트워크", "원격근무", "smart work", "정보 통신 기술을 이용해 시간과 장소의 제약 없이 업무를 수행하는 유연한 근무 형태.", ""},
            {"스마트 워크", "원격근무", "smart work", "정보 통신 기술을 이용해 시간과 장소의 제약 없이 업무를 수행하는 유연한 근무 형태.", ""},
            {"스마트폰", "똑똑(손)전화", "smart phone", "인터넷 정보 검색, 그림 정보 송수신 등의 기능을 갖춘 휴대 전화.", ""},
            {"스마트 폰", "똑똑(손)전화", "smart phone", "인터넷 정보 검색, 그림 정보 송수신 등의 기능을 갖춘 휴대 전화.", ""},
            {"스마트", "똑똑한", "smart", "", ""},
            {"스카이 라운지", "하늘쉼터", "sky lounge", "고층 건물의 맨 위층에 자리한 휴게실, 아주 높은 곳에 편안히 쉴 수 있도록 특별히 마련해 놓은 공간을 이르는 말.", ""},
            {"스크린도어", "안전문", "screen door", "기차나 지하철을 타는 사람이 찻길에 떨어지거나, 열차와 타는 곳 사이에 발이 끼는 따위의 사고를 막기 위해서 설치하는 문.", ""},
            {"스키니진", "맵시청바지", "skinny jean", "허리부터 발목까지 다리에 딱 달라붙는 청바지.", ""},
            {"스킨십", "피부교감", "skinship", "직접적인 피부 접촉을 통하여 서로 간에 사랑의 감정을 주고받는 일.", ""},
            {"스타일리스트", "맵시가꿈이", "stylist", "옷이나 실내 장식 따위와 관련된 일에 조언을 하거나 지도하는 사람.", ""},
            {"스탠더드 넘버", "대중명곡", "standard number", "시대에 관계없이 오랫동안 늘 연주되고 사랑받아 온 곡.", ""},
            {"스테디셀러", "늘사랑상품", "steady seller", "시간과 상관없이 늘 사랑을 받아 왔거나 받고 있는 상품, 한결같이 꾸준히 팔리는 물건을 가리킴.", ""},
            {"스토리보드", "그림줄거리", "storyboard", "이야기에서 중심이 되는 줄기를 이루는 것을 그림으로 옮겨 놓은 것을 가리켜 이름.", ""},
            {"스파이웨어", "정보빼내기 프로그램", "spyware", "다른 사람의 개인 정보를 몰래 빼 가는 프로그램.", ""},
            {"스팸메일", "쓰레기편지", "spam mail", "상품이나 서비스에 대한 정보를 널리 알리기 위하여 많은 사람들에게 마구잡이로 보내는 쓰레기, 공해와 같은 광고.", ""},
            {"스펙업", "깜냥쌓기", "spec-up", "자신의 미래를 위해 좀 더 나은 학력, 학점, 토익 점수, 자격증 등을 취득하기 위해 노력하는 것을 뜻함.", ""},
            {"스포일러", "영화헤살꾼", "spoiler", "아직 영화를 보지 않은 사람에게 영화의 주요한 내용, 특히 결말을 미리 알려서 영화 보는 재미를 크게 떨어뜨리게 하는 사람을 가리킴.\n‘헤살꾼’은 ‘남의 일에 짓궂게 훼방을 놓는 사람’을 뜻하는 순우리말임.", ""},
            {"스포테인먼트", "흥끌이운동", "spotainment", "‘스포츠’와 ‘엔터테인먼트’를 합성한 말로, ‘운동 효과와 오락성을 아울러 갖춘 것(일, 사물)’을 가리킴.", ""},
            {"스팟광고", "반짝광고", "spot廣告", "프로그램을 방송하는 사이에 나가는 광고.", ""},
            {"스폿광고", "반짝광고", "spot廣告", "프로그램을 방송하는 사이에 나가는 광고.", ""},
            {"슬로시티", "참살이지역", "slow city", "느림의 철학을 바탕으로 자연 생태 환경과 전통문화를 지키는 지역민 중심의 공동체를 이르는 말.", ""},
            {"슬로푸드", "여유식", "slow food", "‘만들어서 먹는 데 많은 시간이 걸리는 음식’을 뜻함.", ""},
            {"슬롯머신", "성인오락기", "slot machine", "주로 성인 오락실에서 동전을 집어넣고 화면에 똑같은 그림 세 개가 나오면 더 많은 돈을 돌려받을 수 있는 도박용 게임기를 가리켜 이르는 말.", ""},
            {"시스루", "비침옷", "see-through", "속이 비치는 얇은 옷. ‘시스루패션’, ‘시스루룩’(얇고 비치는 소재로 만드는 양장 스타일의 하나)과 같은 용어로 확장되어 쓰이기도 함.", ""},
            {"CCTV", "상황관찰기", "CCTV, closed circuit TV", "특정 수신자를 대상으로 화상을 전송하는 텔레비전 방식으로, 보통 범죄 예방용이나 도로 교통 상황 관찰 등을 위해 쓰임. ‘폐쇄회로 티브이(TV)’라고도 부름.", ""},
            {"시즌", "계절", "season", "", "~번째 이야기"},
            {"시즈닝", "양념", "seasoning", "양념(특히 소금과 후추)", ""},
            {"실버시터", "경로도우미", "silver sitter", "가족 대신 노인을 보살펴 주는 일을 하는 사람. 또는 그런 직업.", ""},
            {"시티", "도시", "city", "", ""},
            {"싱글맘", "홀보듬엄마", "single mom", "혼자서 아이를 키우는 엄마.", ""},
            {"싱크로율", "일치율", "synchro率", "어떤 요소와 요소가 합쳐지면서 발생하는 것으로 ‘일치율’, ‘완성도’, ‘정확도’ 등과 비슷한 의미로 쓰임.", ""},
            {"아우라", "기품", "Aura", "일반적으로 사람이나 작품 따위에서 드러나는 고상한 품격을 가리킴.", ""},
            {"아우터", "겉차림옷", "outer", "맨 겉에 차려입는 옷.", ""},
            {"아이쇼핑", "눈길장보기", "eye shopping", "물건은 사지 아니하고 눈으로만 보고 즐기는 일.", ""},
            {"아이젠", "눈길덧신", "Eisen", "등산화 바닥에 부착하여 미끄러짐을 방지하는 등산 용구를 통틀어 이르는 말.", ""},
            {"아이콘", "상징(물)", "icon", "‘어떠한 분야의 최고 또는 대표하는 것’을 이르는 말.", "쪽그림"},
            {"IPTV", "맞춤형 누리방송", "IPTV, Internet Protocol Television", "초고속 인터넷망을 이용하여 제공되는 양방향 텔레비전 서비스이며 시청자가 자신이 편리한 시간에 보고 싶은 프로그램을 골라 볼 수 있는 텔레비전. ", ""},
            {"아카이브", "자료전산화", "archive", "소장품이나 자료 등을 디지털화하여 한데 모아서 관리할 뿐만 아니라 그것들을 손쉽게 검색할 수 있도록 하는 일.", ""},
            {"아킬레스건", "치명(적) 약점", "Achilles腱", "‘어떠한 상대의 치명적인 약점’을 통틀어 이르는 말.", ""},
            {"아티젠", "감각세대", "Artygen", "상품에 예술가의 작품이나 디자이너의 작품을 접목시킨 제품을 선호하는 소비 계층을 이르는 말.", ""},
            {"알파걸", "으뜸녀", "alpha girl", "남성보다 능력이 뛰어난 엘리트 여성, 즉 첫째가는 여성을 가리켜 이르는 말.", ""},
            {"알파 걸", "으뜸녀", "alpha girl", "남성보다 능력이 뛰어난 엘리트 여성, 즉 첫째가는 여성을 가리켜 이르는 말.", ""},
            {"언더패스", "아래차로", "underpass", "철도나 다른 도로의 아래를 지나는 도로를 가리켜 이르는 말.", ""},

            {"언론플레이", "여론몰이", "言論play", "주로 정치 또는 연예계에서, 자신의 목적을 위하여 언론을 이용하는 것을 뜻함. 자기에게 이롭게 여론 분위기를 이끌어 가는 일.", ""},
            {"에듀테인먼트", "놀이학습", "edutainment", "오락성을 겸비한 교육용 상품 또는 그러한 성격을 띤 학습 형태. ‘에듀테인먼트’는 교육적인 요소에 오락적인 요소를 가미한 것을 말하는 혼성어임. [교육(education) + 오락(entertainment)]", ""},
            {"엔터테인먼트", "놀이", "entertainment", "", "오락"},
            {"에스라인", "호리병몸매", "s-line", "여성들의 체형을 알파벳 에스(S) 자로 나타낸 것으로, 특히 옆에서 보았을 때 가슴과 엉덩이가 강조되는 풍만하고 늘씬한 몸매.", ""},
            {"S라인", "호리병몸매", "s-line", "여성들의 체형을 알파벳 에스(S) 자로 나타낸 것으로, 특히 옆에서 보았을 때 가슴과 엉덩이가 강조되는 풍만하고 늘씬한 몸매.", ""},
            {"SOS", "구원요청", "SOS - 뜻 없음", "무선 전신을 이용한 조난 신호. 일반적으로는 급하게 구원이나 원조를 요청하는 행위나 말을 가리킴.", ""},
            {"에코드라이브", "친환경 운전", "eco-drive", "‘친환경, 경제성, 안전을 고려한 운전 및 그러한 운전 방식’을 이르는 말.", ""},
            {"에코 드라이브", "친환경 운전", "eco-drive", "‘친환경, 경제성, 안전을 고려한 운전 및 그러한 운전 방식’을 이르는 말.", ""},
            {"에코드라이빙", "친환경 운전", "eco-driving", "‘친환경, 경제성, 안전을 고려한 운전 및 그러한 운전 방식’을 이르는 말.", ""},
            {"에코 드라이빙", "친환경 운전", "eco-driving", "‘친환경, 경제성, 안전을 고려한 운전 및 그러한 운전 방식’을 이르는 말.", ""},
            {"에코맘", "환경친화주부", "EcoMom", "환경 보호를 생활에서 실천하는 가정주부. 즉 가정에서 생태주의적인 삶을 추구하는 주부.", ""},
            {"에코", "환경", "eco", "‘환경·생태’와 관련됨을 나타냄", "울림, 메아리 - echo"},
            {"엑스파일", "안개문서", "X file", "‘아직 결정하거나 해결하지 않은 사건에 관한 문서나 서류’ 또는 ‘아직 알지 못하여 사실 여부가 확인되지 않은 일이나 사건에 관한 문서나 서류’.", ""},
            {"엔딩크레딧", "끝맺음자막", "ending credit", "영화나 드라마 등의 끝부분에 제시되는 제작에 참여한 사람들을 소개하는 자막.", ""},
            {"NG족", "늑장 졸업족", "NG族, No Graduation族", "충분히 졸업할 수 있는 여건이 되었음에도 취업, 진로 등의 문제로 졸업을 미루는 학생.", ""},
            {"엔지족", "늑장 졸업족", "NG族, No Graduation族", "충분히 졸업할 수 있는 여건이 되었음에도 취업, 진로 등의 문제로 졸업을 미루는 학생.", ""},

            {"엠니스", "주부남", "M-ness", "힘과 명예를 추구하는 등의 전통적인 남성상에다 대화, 배려, 협력, 양육 등의 여성스러운 긍정적 특성을 아울러 갖춘 남성.", ""},
            {"영건", "기대주", "young gun", "보통 20대 초반에서 20대 중반까지의 선수를 가리키는 말.", ""},
            {"예티족", "자기가치 개발족", "Yettie族", "‘젊고(Young)’,‘기업가답고(Entrepreneurial)’ ‘기술에 바탕을 둔(Technic-based)’, ‘인터넷 엘리트(Internet Elite)’를 의미하며, 정보 기술을 선도하는 디지털 시대의 새로운 인간군(人間群)을 이르는 말.", ""},
            {"오마주", "감동되살이", "hommage", "주로 영화에서 존경의 표시로 다른 작품의 주요 장면이나 대사를 인용하는 일을 가리킴.", ""},
            {"오버페이스", "무리", "over pace", "운동 경기나 어떤 일을 할 때에 자기 능력이나 분수 이상으로 무리하게 하는 것.", ""},
            {"오버 페이스", "무리", "over pace", "운동 경기나 어떤 일을 할 때에 자기 능력이나 분수 이상으로 무리하게 하는 것.", ""},
            {"오일볼", "기름뭉치", "oil ball", "바다 위에 유출된 원유나 폐유가 표류하다가 표면이 굳어져 덩어리 모양으로 엉겨 붙은 것.", ""},
            {"오프라인", "현실공간", "off-line", "‘인터넷과 같은 가상공간이 아닌 실재하는 공간, 또는 사람들이 실제로 경험하는 현실의 세계’를 가리키는 말.", ""},
            {"오픈마켓", "열린장터", "Open Market", "인터넷에서 판매자와 구매자를 직접 연결하여 자유롭게 물건을 사고 팔 수 있는 곳.", ""},
            {"오픈 마켓", "열린장터", "Open Market", "인터넷에서 판매자와 구매자를 직접 연결하여 자유롭게 물건을 사고 팔 수 있는 곳.", ""},
            {"오픈프라이스", "열린 가격제", "open price 制", "제조 업체가 결정하는 권장 소비자 가격 표시를 금지하고, 판매업자가 자율적으로 판매 가격을 결정해 표시하는 제도.", ""},
            {"오픈 프라이스", "열린 가격제", "open price 制", "제조 업체가 결정하는 권장 소비자 가격 표시를 금지하고, 판매업자가 자율적으로 판매 가격을 결정해 표시하는 제도.", ""},
            {"오픈", "열린", "open", "'열다' 혹은 '열린' 정도로 해석 가능", ""},
            {"오픈하우스", "열린 집", "open house", "건설사나 건설업자가 본보기집(모델 하우스)이나 체험관 같은 곳을 만들어서 집을 사고자 하는 사람이라면 누구든지 들어와서 보거나 체험할 수 있도록 하는 일, 또는 그런 경우. ", ""},
            {"오픈 하우스", "열린 집", "open house", "건설사나 건설업자가 본보기집(모델 하우스)이나 체험관 같은 곳을 만들어서 집을 사고자 하는 사람이라면 누구든지 들어와서 보거나 체험할 수 있도록 하는 일, 또는 그런 경우. ", ""},
            {"모델하우스", "본보기집", "model house", "아파트 따위를 지을 때, 집을 사고자 하는 사람에게 미리 보이기 위하여 실제 내부와 똑같게 지어 놓은 집.", ""},
            {"모델 하우스", "본보기집", "model house", "아파트 따위를 지을 때, 집을 사고자 하는 사람에게 미리 보이기 위하여 실제 내부와 똑같게 지어 놓은 집.", ""},
            {"올인", "다걸기", "all in", "도박 따위에서 자기가 가지고 있는 판돈을 모두 거는 행위’ 또는 ‘선거나 정책 따위에서 앞뒤 가리지 않고 자기 조직의 모든 힘을 쏟아 붓는 것’.", ""},
            {"올 인", "다걸기", "all in", "도박 따위에서 자기가 가지고 있는 판돈을 모두 거는 행위’ 또는 ‘선거나 정책 따위에서 앞뒤 가리지 않고 자기 조직의 모든 힘을 쏟아 붓는 것’.", ""},
            {"올킬", "싹쓸이", "all kill", "연예, 게임, 스포츠 등에서 ‘석권(席卷, 席捲)’, ‘전승(全勝)’ 등을 이르는 말.", ""},
            {"올 킬", "싹쓸이", "all kill", "연예, 게임, 스포츠 등에서 ‘석권(席卷, 席捲)’, ‘전승(全勝)’ 등을 이르는 말.", ""},
            {"옴부즈맨", "민원도우미", "ombudsman", "‘옴부즈맨’은 본래 ‘대리자’, ‘대리인’이라는 뜻의 스웨덴어에서 온 말임. 요즘에는 정부, 공공기관 등에 대하여 일반 국민이 갖는 불평이나 불만을 처리하는 사람이란 뜻으로 흔히 쓰임.", "국민 감사단 / 시민 의견 청취 제도"},
            {"와이브로", "휴대누리망", "WiBro", "휴대 전화로 언제 어디서나 손쉽게 인터넷을 이용하는 일. 또는 그런 기술.", ""},
            {"와이파이", "Wi-Fi", "근거리 무선망", "무선 인터넷이 개방된 장소에서 무선접속장치(AP)가 설치된 곳을 중심으로 일정 거리 이내에서 똑똑전화(스마트폰)나 노트북 등을 통하여 초고속 인터넷을 이용할 수 있는 설비.", ""},
            {"워킹맘", "직장인 엄마", "working mom", "‘아이를 낳아 기르면서 일을 하는 여성’을 통틀어 이르는 말.", ""},
            {"워킹 홀리데이", "관광 취업", "working holiday", "국가 간 비자 협정을 통해 상대국 청소년(통상 만 18~30세)들이 자유롭게 취업하며 관광이나 연수를 할 수 있도록 허가하는 제도.", ""},
            {"워킹홀리데이", "관광 취업", "working holiday", "국가 간 비자 협정을 통해 상대국 청소년(통상 만 18~30세)들이 자유롭게 취업하며 관광이나 연수를 할 수 있도록 허가하는 제도.", ""},
            {"워홀", "관광 취업", "working holiday", "국가 간 비자 협정을 통해 상대국 청소년(통상 만 18~30세)들이 자유롭게 취업하며 관광이나 연수를 할 수 있도록 허가하는 제도.", ""},
            {"워터파크", "물놀이 공원", "water park", "물놀이 따위를 위하여 마련한 공공시설.", ""},
            {"원샷", "한입털이", "one shot", "술잔에 남아 있는 술을 한 번 입을 벌려 남김없이 다 마셔 버리는 일.", ""},
            {"원 샷", "한입털이", "one shot", "술잔에 남아 있는 술을 한 번 입을 벌려 남김없이 다 마셔 버리는 일.", ""},
            {"원톱", "홀로주연", "one top", "영화나 드라마 따위에서 홀로 주연을 맡아서 극의 전반적인 흐름을 책임지는 배우. 또는 그런 일.", ""},
            {"원 톱", "홀로주연", "one top", "영화나 드라마 따위에서 홀로 주연을 맡아서 극의 전반적인 흐름을 책임지는 배우. 또는 그런 일.", ""},
            {"월풀", "공깃방울 목욕", "whirlpool", "터빈을 이용해 욕조의 벽면이나 바닥 등에서 물이 분사되는 기능 또는 그런 기능을 이용한 목욕", ""},
            {"웨딩 플래너", "결혼도우미", "wedding planner", "결혼을 뜻하는 말인 ‘웨딩’(wedding)과 계획해 주는 사람을 뜻하는 말인 ‘플래너’(planner)가 합하여 만들어진 말. 결혼 예정자를 대상으로 결혼에 관한 모든 것을 준비하고 신랑 신부의 일정 관리와 각종 절차·예산 등을 기획하고 대행해 주는 일을 하는 사람.", ""},
            {"웨딩플래너", "결혼도우미", "wedding planner", "결혼을 뜻하는 말인 ‘웨딩’(wedding)과 계획해 주는 사람을 뜻하는 말인 ‘플래너’(planner)가 합하여 만들어진 말. 결혼 예정자를 대상으로 결혼에 관한 모든 것을 준비하고 신랑 신부의 일정 관리와 각종 절차·예산 등을 기획하고 대행해 주는 일을 하는 사람.", ""},
            {"웰본", "배냇바라지", "well-born", "열풍이 불었던 참살이(웰빙, wellbeing) 바람에서 한 걸음 더 나아가, ‘아기가 태어나기 전부터 건강 관리, 섭생, 태교 등에 각종 투자를 아끼지 않는 사회 현상’을 가리켜 이르는 말.", ""},
            {"웰 본", "배냇바라지", "well-born", "열풍이 불었던 참살이(웰빙, wellbeing) 바람에서 한 걸음 더 나아가, ‘아기가 태어나기 전부터 건강 관리, 섭생, 태교 등에 각종 투자를 아끼지 않는 사회 현상’을 가리켜 이르는 말.", ""},
            {"웰빙", "참살이", "well-being", "몸과 마음의 안녕과 행복 또는 그것을 추구하는 일을 가리킴. 웰빙(참살이)을 추구하는 사람들은 대개 육류 대신 생선과 유기 농산물을 즐기고, 단전 호흡이나 요가 등의 마음을 안정시킬 수 있는 운동을 하며, 외식을 삼가고 가정에서 만든 음식을 즐겨 먹고, 여행·등산 등의 취미 생활을 즐기는 경향을 보임.", ""},
            {"웰 빙", "참살이", "well-being", "몸과 마음의 안녕과 행복 또는 그것을 추구하는 일을 가리킴. 웰빙(참살이)을 추구하는 사람들은 대개 육류 대신 생선과 유기 농산물을 즐기고, 단전 호흡이나 요가 등의 마음을 안정시킬 수 있는 운동을 하며, 외식을 삼가고 가정에서 만든 음식을 즐겨 먹고, 여행·등산 등의 취미 생활을 즐기는 경향을 보임.", ""},
            {"웹버", "은빛누리꾼", "Webver族", "‘웹(web)’과 노인 세대를 지칭하는 ‘실버(silver)족’을 합친 말로, ‘인터넷을 적극적으로 활용하고 즐기는 노년층’을 가리켜 이르는 말.", ""},
            {"웹서핑", "누리검색", "web surfing", "흥밋거리를 찾아 인터넷에 개설된 여러 사이트에 이리저리 접속하는 일을 가리켜 이르는 말.", ""},
            {"웹 서핑", "누리검색", "web surfing", "흥밋거리를 찾아 인터넷에 개설된 여러 사이트에 이리저리 접속하는 일을 가리켜 이르는 말.", ""},
            {"웹툰", "누리터쪽그림", "Webtoon", "웹(web)과 카툰(cartoon)의 합성어로 인터넷 웹상에서 연재되는 만화.", ""},
            {"유비쿼터스", "두루누리", "Ubiquitous", "어디서나 어떤 기기로든 자유롭게 통신망에 접속하여 갖은 자료들을 주고받을 수 있는 환경.", ""},
            {"UCC","손수제작물",  "UCC - User Created Contents", "정보나 볼거리의 이용자 또는 소비자인 시청자나 누리꾼이 직접 생산·제작하는 콘텐츠(꾸림정보)를 가리켜 이르는 말.", ""},
            {"USB", "정보막대", "USB - Universal Serial Bus", "유에스비(USB) 포트에 꽂아 쓰는 플래시 메모리를 이용한 이동형 저장 장치를 통틀어 이르는 말.", ""},
            {"이모티콘", "그림말", "emoticon", "컴퓨터 자판의 각종 기호와 글자를 조합해서 감정, 모양, 소리 따위를 그림처럼 나타내는 것.", ""},
            {"인저리타임", "추가시간", "injury time", "축구 경기에서 전·후반 각 45분의 정규 시간 이후 주심이 재량에 따라 추가로 허용하는 시간.", ""},
            {"정크푸드", "부실음식", "junk food", "열량은 높지만 영양가는 낮은 즉석식(패스트푸드)과 즉석식품(인스턴트식품)을 통틀어 이르는 말.", ""},
            {"정크 푸드", "부실음식", "junk food", "열량은 높지만 영양가는 낮은 즉석식(패스트푸드)과 즉석식품(인스턴트식품)을 통틀어 이르는 말.", ""},
            {"제로베이스", "백지상태", "zero base", "무엇인가 해당되는 것이 전혀 존재하지 않는 상태, 또는 그런 상태를 가정하는 것. ", ""},
            {"제로 베이스", "백지상태", "zero base", "무엇인가 해당되는 것이 전혀 존재하지 않는 상태, 또는 그런 상태를 가정하는 것. ", ""},
            {"푸드", "음식", "food", "", ""},
            {"쪼리", "가락신", "ぞうり[草履]", "엄지발가락과 둘째발가락 사이에 끈을 끼워서 신는 일본식 여름 신발. 짚·골풀 등으로 엮은, 바닥이 평평하고, ‘게다’와 같은 끈을 단 일본식 신발.", ""},
            {"쬬리", "가락신", "ぞうり[草履]", "엄지발가락과 둘째발가락 사이에 끈을 끼워서 신는 일본식 여름 신발. 짚·골풀 등으로 엮은, 바닥이 평평하고, ‘게다’와 같은 끈을 단 일본식 신발.", ""},
            {"체리피커", "금융얌체족", "cherry picker", "주로 신용 카드 회사나 은행 등에서 제공하는 특별한 혜택만 누리고는 정작 신용 카드는 사용하지 않거나 금융 상품에는 가입하지 않는 사람을 가리켜 이르는 말.", ""},
            {"치어리더", "흥돋움이", "cheerleader", "운동 경기에서, 관중의 흥을 돋우기 위하여 흥겨운 음악에 맞추어 춤을 추거나 응원 구호를 외치는 사람.", ""},
            {"치킨게임", "끝장승부", "chicken game", "어떠한 문제를 둘러싸고 대립하는 상황에서 서로가 양보 없이 극한까지 몰고 가는 상황.", ""},
            {"치킨 게임", "끝장승부", "chicken game", "어떠한 문제를 둘러싸고 대립하는 상황에서 서로가 양보 없이 극한까지 몰고 가는 상황.", ""},
            {"게임", "놀이", "game", "규칙을 정해 놓고 승부를 겨루는 놀이.", ""},
            {"칙릿", "꽃띠문학", "chick-lit", "새로운 여성 문학 작품의 하나로 크게 각광받고 있는데 20대 여성들의 이야기를 담은 문학 작품. 특히, 소설을 가리켜 이르는 ‘칙릿(chick-lit)’은 한창 젊은 여자를 겨냥하거나 소재로 하는 문학.", ""},
            {"카시트", "안전 의자", "car seat", "아이들의 안전을 위해 차량 좌석에 설치하여 사용하는, 아이들 체형에 맞는 의자.", ""},
            {"카 시트", "안전 의자", "car seat", "아이들의 안전을 위해 차량 좌석에 설치하여 사용하는, 아이들 체형에 맞는 의자.", ""},
            {"베이비시트", "유아용 의자", "baby seat", "아이들의 안전을 위해 차량 좌석에 설치하여 사용하는, 아이들 체형에 맞는 의자.", ""},
            {"베이비 시트", "유아용 의자", "baby seat", "아이들의 안전을 위해 차량 좌석에 설치하여 사용하는, 아이들 체형에 맞는 의자.", ""},
            {"헝그리정신", "맨주먹정신", "hungry 精神", "‘끼니를 잇지 못할 만큼 어려운 상황에서도 꿋꿋한 의지로 역경을 헤쳐 나가는 정신’을 비유적으로 이르는 말.", ""},
            {"헝그리 정신", "맨주먹정신", "hungry 精神", "‘끼니를 잇지 못할 만큼 어려운 상황에서도 꿋꿋한 의지로 역경을 헤쳐 나가는 정신’을 비유적으로 이르는 말.", ""},
            {"헤드셋", "통신 머리띠", "headset", "마이크가 달린 헤드폰을 가리킴.", ""},
            {"헤비업로더", "누리물난전꾼", "heavy uploader", "웹하드, 피투피(P2P) 등 온라인을 통해 영리적 목적으로 불법 저작물을 전송해 이득을 챙기는 사람.", ""},
            {"헬리콥터부모", "치마폭부모", "helicopter 父母", "헬리콥터 프로펠러처럼 자녀 주변을 맴돌며 간섭하는 부모를 가리켜 이르는 말.", ""},
            {"헬리콥터 부모", "치마폭부모", "helicopter 父母", "헬리콥터 프로펠러처럼 자녀 주변을 맴돌며 간섭하는 부모를 가리켜 이르는 말.", ""},
            {"호스피스", "임종봉사자", "hospice", "죽음을 앞둔 환자에게 평안한 임종을 맞도록 위안과 안락을 베푸는 봉사 활동을 하는 사람.", ""},
            {"홀드", "잡다", "hold", "", "중간구원 - 야구용어"},
            {"홈베이킹", "손수굽기", "home baking", "가정이나 집에서 빵이나 과자를 직접 구워 먹는 일.", ""},
            {"후루꾸", "어중치기", "フロック", "진짜가 아니거나 실제와 다른 것’을 비아냥거리거나 속된 뜻으로 이를 때 사용하는 말.", ""},
            {"후루쿠", "어중치기", "フロック", "진짜가 아니거나 실제와 다른 것’을 비아냥거리거나 속된 뜻으로 이를 때 사용하는 말.", ""},
            {"후룻꾸", "어중치기", "フロック", "진짜가 아니거나 실제와 다른 것’을 비아냥거리거나 속된 뜻으로 이를 때 사용하는 말.", ""},
            {"후롯쿠", "어중치기", "フロック", "진짜가 아니거나 실제와 다른 것’을 비아냥거리거나 속된 뜻으로 이를 때 사용하는 말.", ""},
            {"후카시", "품재기", "ふかし", "품재기: ‘품’(행동이나 말씨에서 드러나는 태도나 됨됨이)과 ‘재기’[‘재다’(잘난 척하며 으스대거나 뽐내다)의 명사형]의 합성어.", ""},
            {"후크송", "맴돌이곡", "Hook Song", "한 노래에 같은 가사를 여러 번 반복적으로 사용하여 만든 노래.", ""},
            {"휘핑", "거품크림", "whipping", "커피 전문점에서, 커피 위에 올려놓는 크림.", ""},
            {"휴테크", "여가활용기술", "休tech", "휴식과 여가 시간을 활용하여 창의력을 키우고 자기 계발을 함으로써 경쟁력을 키우기 위해 하는 일.", ""},
            {"히키코모리", "폐쇄은둔족", "引き籠もり", "사회생활에 적응하지 못하고 집안에만 틀어박혀 사는 사람들, 또는 그런 현상’을 가리켜 이르는 말.", ""},






    };

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();

        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {


        if (status == TextToSpeech.SUCCESS) {
            int language = tts.setLanguage(Locale.KOREAN);

            if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                btnTTSRead.setEnabled(false);
                btnTTSRead2.setEnabled(false);
                Toast.makeText(getActivity(), "지원하지 않는 언어입니다.", Toast.LENGTH_SHORT).show();
            }
            else {
                btnTTSRead.setEnabled(true);
                btnTTSRead2.setEnabled(true);
            }
        } else {
            Toast.makeText(getActivity(), "TTS 실패!", Toast.LENGTH_SHORT).show();
        }

    }

    public void TranslateClick(View view) {
        Toast.makeText(getActivity(), "1234", Toast.LENGTH_LONG).show();
        btnTTSRead.setText("1234");


    }


    private void speakOutNow() {
        String text = textConvertResult.getText().toString();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    private void speakOutNow2() {
        String text2 = textConvertInput.getText().toString();
        tts.speak(text2, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void clicked()
    {
       // Toast.makeText(getActivity(), "뭐라도 보여드리겠습니다!", Toast.LENGTH_LONG).show();
        //System.out.println("눈물의 똥꼬쇼");
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void Translate()
    {

        //0731 처음부터 시작
        String Translate = textConvertInput.getText().toString();
        System.out.println(ChangeArr.length);
        int[][] changed =
                {
                        {0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},
                        {0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},
                        {0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},
                        {0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},
                        {0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},
                        {0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},
                        {0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},
                        {0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},
                        {0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},
                        {0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},
                        {0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},
                        {0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},{0, 0},
                };


        int changedLastIndex = 0;
        for(int i = 0; i<ChangeArr.length; i++)
        {
            Translate = Translate.replaceAll(ChangeArr[i][0], ChangeArr[i][1]);
        }
        for(int i = 0; i<ChangeArr.length; i++)
        {
           // Translate = Translate.replaceAll(ChangeArr[i][0], ChangeArr[i][1]);
            if(Translate.contains(ChangeArr[i][1]))
            {
                String a = ChangeArr[i][1];
                System.out.println(a);
                changed[changedLastIndex][0] = Translate.indexOf(a);
                changed[changedLastIndex][1] = a.length();
                System.out.println(changed[changedLastIndex][0]);
                System.out.println(changed[changedLastIndex][1]);
                changedLastIndex++;
                System.out.println(changedLastIndex + "★★★★★★★★★★★★");
            }
           // System.out.println(Translate);


        }
        SpannableString spannableString = new SpannableString(Translate);

        int z = 0;


        for(z=0; z<=changedLastIndex; z++)
        {
            //System.out.println("＠@@@@@@@@@@" + changed[z][1]);
                int finalZ = z;

            String finalTranslate = Translate;
            ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View view) {
                        //Toast.makeText(getActivity(), finalTranslate.substring(changed[finalZ][0], changed[finalZ][0]+changed[finalZ][1]), Toast.LENGTH_LONG).show();

                       AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        String WordSet = finalTranslate.substring(changed[finalZ][0], changed[finalZ][0]+changed[finalZ][1]);
                        System.out.println("＠@@@@@@@@@@" + WordSet);

                        int WordMatch = 0;
                        while(!WordSet.equals(ChangeArr[WordMatch][1]))
                        {
                            WordMatch++;
                        }


                       builder.setTitle(WordSet).setMessage("원래 단어 | " + ChangeArr[WordMatch][0] + " - " + ChangeArr[WordMatch][2] + "\n\n" + "의미 | " + ChangeArr[WordMatch][3] + "\n\n" + "다른 단어 | " + ChangeArr[WordMatch][4] + "\n");

                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                               // Toast.makeText(getActivity().getApplicationContext(), "OK Click", Toast.LENGTH_SHORT).show();
                            }
                        });
                        /*
                        builder.setNegativeButton("수정 제안", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                                //뭐 하던가 나중에 지우던가
                                Toast.makeText(getActivity().getApplicationContext(), "의견을 보내주셔서 감사합니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        */
                        int finalWordMatch = WordMatch;
                        builder.setNeutralButton("공유하기", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {

                                //String Test_Message = getTextBeforeChange() + "[" + data.getTextEnglish()+ "]의 우리말은 '"+ data.getTextAfterChange() + "'이고, \n뜻은 '" + data.getTextMeaning()+"'입니다!\n\n건전한 우리말 생활, '띠앗'과 함께 만들어가요!\nhttps://tinyurl.com/ti-att/";

                                Intent Sharing_intent = new Intent(Intent.ACTION_SEND);
                                Sharing_intent.setType("text/plain");

                                String Test_Message = ChangeArr[finalWordMatch][0]  + "[" + ChangeArr[finalWordMatch][2] + "]의 우리말은 '"+ ChangeArr[finalWordMatch][1] + "'이고, \n뜻은 '" + ChangeArr[finalWordMatch][3] + "'입니다!\n건전한 우리말 생활, '띠앗'과 함께 만들어가요!\nhttps://tinyurl.com/ti-att/";

                                Sharing_intent.putExtra(Intent.EXTRA_TEXT, Test_Message);

                                Intent Sharing = Intent.createChooser(Sharing_intent, "공유하기");
                                startActivity(Sharing);
                                //Toast.makeText(getActivity().getApplicationContext(), "Neutral Click", Toast.LENGTH_SHORT).show();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        System.out.println("눈물의 똥꼬쇼");
                    }
                };


                spannableString.setSpan(clickableSpan, changed[z][0], changed[z][0]+changed[z][1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);





        }


        textConvertResult.setText(spannableString);
        //textConvertResult.setText(Translate);
        textConvertResult.setMovementMethod(LinkMovementMethod.getInstance());





        // 기존코드
        /*
        String translateSource = textConvertInput.getText().toString();
        String[] translateWord = translateSource.split(" ");
        String translateFinishWord = "";

        for(int i = 0; i<translateWord.length; i++)
        {
            for(int j = 0; j<ChangeArr.length; j++)
            {
                if(translateWord[i].equals(ChangeArr[j][0]))
                {
                    translateWord[i] = ChangeArr[j][1];

                }
            }

            translateFinishWord = String.format("%s %s", translateFinishWord, translateWord[i]);
        }
        textConvertResult.setText(translateFinishWord);
        //new ScrollingMovementMethod();
       // textConvertResult.setMovementMethod(ScrollingMovementMethod.getInstance());

    */



        /*
        //수정코드
        String translateSource = textConvertInput.getText().toString();
        //System.out.println(translateSource);
        String[] translateWord = translateSource.split("");
        //System.out.println(translateWord[0]);
        int changeWordIndex[] = new int[10]; // 제출 전 수정
        int changeWordLength[] = new int[10]; // 제출 전 수정
        int changeWordPlus[] = new int[10];  // 제출 전 수정
        String translateFinishWord = "";
        int k = 0;
        for(int i = 1; i<ChangeArr.length; i++)
        {

            if(translateSource.contains(ChangeArr[i][0]))
            {
                int translateRepeat = -1;
                for(int n = 0; n<translateWord.length; n++)
                {
                    if(translateSource.indexOf(ChangeArr[i][0], n) != -1)
                    {
                        if(translateRepeat == translateSource.indexOf(ChangeArr[i][0], n))
                        {
                            continue;
                        }
                        else
                        {
                            translateRepeat = translateSource.indexOf(ChangeArr[i][0], n);
                            translateWord[translateSource.indexOf(ChangeArr[i][0], n)] = ChangeArr[i][1];
                            String changestr = ChangeArr[i][0];
                            String changedstr = ChangeArr[i][1];
                            for(int j = 0; j<changestr.length(); j++)
                            {
                                translateWord[translateSource.indexOf(ChangeArr[i][0], n)+j] = "";
                            }
                            translateWord[translateSource.indexOf(ChangeArr[i][0], n)] = changedstr;
                            changeWordIndex[k] = translateSource.indexOf(ChangeArr[i][0], n);
                            changeWordLength[k] = changedstr.length();
                            changeWordPlus[k] = changedstr.length() - changestr.length();
                            k++;
                        }
                    }
                    else
                    {
                        translateRepeat = -1;
                        break;
                    }
                }
            }

        }
        System.out.println(translateWord[0]);
        translateFinishWord = Arrays.toString(translateWord);
        translateFinishWord = translateFinishWord.replace("[", "");
        translateFinishWord = translateFinishWord.replace("]", "");
        translateFinishWord = translateFinishWord.replace(", ", "");

        System.out.println(translateFinishWord);
      //  System.out.println(translateFinishWord[1]);
        //System.out.println(translateWord[2]);


        int[] WordIndex = new int[10000];
        int[] WordLength = new int[10000];


        int k2 = 0;

        for(int i = 1; i<ChangeArr.length; i++)
        {
            //System.out.println("포문을 열어라~");
            //System.out.println("※" + ChangeArr[i][1]);
            if(translateFinishWord.contains(ChangeArr[i][1]))
            {
                System.out.println("if문");
                int translateRepeat = -1;
                for(int n = 0; n<translateFinishWord.length(); n++) // 될지 모르겠다 -> 아 되긴 하것네
                {
                    if(translateFinishWord.indexOf(ChangeArr[i][1], n) != -1)
                    {
                        if(translateRepeat == translateFinishWord.indexOf(ChangeArr[i][1], n))
                        {
                            continue;
                        }
                        else
                        {
                            translateRepeat = translateSource.indexOf(ChangeArr[i][1], n);
                            WordIndex[k2] = translateSource.indexOf(ChangeArr[i][1], n);
                            WordLength[k2] = ChangeArr[i][1].length();
                            System.out.println(Arrays.toString(WordIndex));
                            k2++;
                        }
                    }
                    else
                    {
                        translateRepeat = -1;
                        break;
                    }
                }
            }

        }

        SpannableString spannableString = new SpannableString(translateFinishWord);
        System.out.println(translateFinishWord);
        System.out.println(spannableString);
        System.out.println(Arrays.toString(WordIndex));
        System.out.println(Arrays.toString(WordLength));
        

        //기존코드 (원래 배열로 받아왔던것)
        int z = 0;
        while(true)
        {
            if(WordLength[z] == 0)
            {
                break;
            }
            else
            {
                int finalZ = z; // debug
                int finalZ1 = z;
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View view) {
                        Toast.makeText(getActivity(), textConvertResult.getText().toString().substring(WordIndex[finalZ1], WordIndex[finalZ1]+WordLength[finalZ1]), Toast.LENGTH_LONG).show();
                        System.out.println("눈물의 똥꼬쇼");
                    }
                };


                spannableString.setSpan(clickableSpan, WordIndex[z], WordIndex[z]+WordLength[z], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);



                z++;
            }
        }


        textConvertResult.setText(spannableString);
        textConvertResult.setMovementMethod(LinkMovementMethod.getInstance());

    // 디버그용
        TextView textView13 = getView().findViewById(R.id.textView13);
        TextView textView14 = getView().findViewById(R.id.textView14);

        textView13.setText(Arrays.toString(changeWordIndex));
        textView14.setText(Arrays.toString(changeWordLength));

*/
        //테스트용
        String CHANNEL_ID = "channel1";
        String CHANEL_NAME = "Channel1";

        NotificationCompat.Builder builder = null;
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(
                new NotificationChannel(CHANNEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        );

        builder = new NotificationCompat.Builder(getActivity().getApplicationContext(), CHANNEL_ID);

        Intent intent = new Intent(getActivity().getApplicationContext(), SlideshowFragment.class);

         final String REPLY_KEY = "reply";
         final String REPLY_LABEL = "ScriptGroup.Input reply"; // Action 에 표시되는 Label
        //RemoteInput remoteInput = new RemoteInput.Builder(REPLY_KEY).setLabel(REPLY_LABEL).build();



        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity().getApplicationContext(), 101, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentTitle("알림");
        builder.setContentText("알림 메시지1");
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
       // manager.notify(1,notification);






    }

}
