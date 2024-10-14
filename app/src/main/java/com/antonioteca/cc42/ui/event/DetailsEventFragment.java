package com.antonioteca.cc42.ui.event;

import static com.antonioteca.cc42.utility.Util.setMarkdownText;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.antonioteca.cc42.databinding.FragmentDetailsEventBinding;

public class DetailsEventFragment extends Fragment {

    private FragmentDetailsEventBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailsEventBinding.inflate(inflater, container, false);
        String formattedText = "We are happy to announce the **42 Prague x Škoda Auto Hackathon!** Here are the logistical deets:\r\n- **Location**: 42 Prague campus, AFI City Tower, Kolbenova 1021/9, 190 00 Prague, Czechia\r\n- **Time**: October 11th, 14th, and 15th\r\n- **Teams**: 3-5 people\r\n\r\nIt’s quite open based on your interests! You will be provided with datasets. It’s your task to come up with an improvement of the Skoda Connect / MySkoda app, or even with a completely new use case for the data. This can be related to AntiTheft features or any other area. What your solution looks like is entirely up to you, so don’t be afraid to be creative. This could be presented in a business format, like a SWOT analysis or case study. It could be another way, like a proposal of a design solution with mock-ups, a demonstration of the use case, or any other way you can describe your solution. \r\nThe event will feature networking, hacking, pitching, and an awards ceremony. So this will be a great chance to develop yourself in all of these areas.\r\n\r\n**[You can read this guide](https://docs.google.com/document/d/1ZjkNiR-cqDeCDNr-ckDXbFVhtmfNCYoio2srqNbwBSg/edit?usp=sharing)** for all of the event specifics and timeline, but I'll list a few important pieces of information below. You'll apply via the registration form linked in the guide above.\r\n\r\nThis is open to 42 Prague students and to other 42 campuses worldwide. **Applications are filled on a first-come, first-served basis** (with the exception of the team per outside campus rule). There are 40 available spaces for 42 Prague students and 20 spaces for students from 42 campuses worldwide.\r\n\r\nYou can join as a team or individual, where you could potentially join people from other campuses on the first day of the hackathon (October 11). The ultimate deadline for registration is **September 30th**.\r\n\r\nYou can direct any questions to Brian (brian@42prague.com) or Lucia (lucia@42prague.com)\r\n\r\n[GOOGLE CALENDAR LINK](https://calendar.google.com/calendar/render?action=TEMPLATE\u0026text=42+Prague+x+%C5%A0koda+Auto+Hackathon\u0026dates=20241011T080000Z%2F20241015T160000Z\u0026details=We+are+happy+to+announce+%2A%2A42+Prague+x+%C5%A0koda+Auto+Hackathon%21%2A%2A+Here+are+the+logistical+deets%3A%0D%0A-+%2A%2ALocation%2A%2A%3A+42+Prague+campus%2C+AFI+City+Tower%2C+Kolbenova+1021%2F9%2C+190+00+Prague%2C+Czechia%0D%0A-+%2A%2ATime%2A%2A%3A+October+11th%2C+14th%2C+and+15th%0D%0A-+%2A%2ATopic%2A%2A%3A+Smart+Theft+Prevention+for+%C5%A0koda%0D%0A-+%2A%2ATeams%2A%2A%3A+3-5+people%0D%0A%0D%0AYour+task+is+to+design+an+innovative+solution+to+enhance+theft+prevention+and+user+notifications+for+%C5%A0koda+vehicles.+Your+mission+is+to+creatively+utilize+the+%C5%A0koda+Connect+app+and+develop+effective+notification+methods+like+robotic+phone+calls.+Improve+the+user+experience+and+security+by+addressing+current+limitations+in+GPS+modules+and+app+alerts.+Imagine+your+solution+as+an+integral+part+of+the+%C5%A0koda+ecosystem%2C+providing+seamless+and+reliable+protection+for+car+owners.%0D%0AThe+event+will+feature+networking%2C+hacking%2C+pitching%2C+and+an+awards+ceremony.+So+this+will+be+a+great+chance+to+develop+yourself+in+all+of+these+areas.%0D%0A%0D%0A%2A%2A%5BYou+can+read+this+guide%5D%28https%3A%2F%2Fdocs.google.com%2Fdocument%2Fd%2F1ZjkNiR-cqDeCDNr-ckDXbFVhtmfNCYoio2srqNbwBSg%2Fedit%3Fusp%3Dsharing%29%2A%2A+for+all+of+the+event+specifics+and+timeline%2C+but+I%27ll+list+a+few+important+pieces+of+information+below.+You%27ll+apply+via+the+registration+form+linked+in+the+guide+above.%0D%0A%0D%0AThis+is+open+to+42+Prague+students+and+to+other+42+campuses+worldwide.+%2A%2AApplications+are+filled+on+a+first-come%2C+first-served+basis%2A%2A+%28with+the+exception+of+the+team+per+outside+campus+rule%29.+There+are+40+available+spaces+for+42+Prague+students+and+20+spaces+for+students+from+42+campuses+worldwide.%0D%0A%0D%0AYou+can+join+as+a+team+or+individual%2C+where+you+could+potentially+join+people+from+other+campuses+on+the+first+day+of+the+hackathon+%28October+11%29.+The+ultimate+deadline+for+registration+is+%2A%2ASeptember+30th%2A%2A.%0D%0A%0D%0AYou+can+direct+any+questions+to+Brian+%28brian%4042prague.com%29+or+Lucia+%28lucia%4042prague.com%29\u0026location=42+Prague+Campus)";
        setMarkdownText(binding.textViewDescription, formattedText);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}